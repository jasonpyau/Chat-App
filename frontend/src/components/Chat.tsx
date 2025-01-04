import React, { ChangeEvent, useEffect, useRef, useState } from 'react';
import "../css/global.css";
import { GroupChat } from '../types/GroupChat';
import SockJS from 'sockjs-client';
import Stomp, { Client, Subscription } from 'stompjs';
import { Message } from '../types/Message';
import MessageView from './MessageView';
import InfiniteScroll from 'react-infinite-scroll-component';
import axios, { AxiosError, AxiosResponse } from 'axios';
import { checkError, checkRedirect } from '../App';
import UsersView from './UsersView';
import { User } from '../types/User';
import { filesize } from 'filesize';


interface ChatProp {
    groupChat: GroupChat,
    refreshChats: (fetch: boolean) => void,
    user: User,
    setTab: (tab: string) => void
}

const Chat: React.FC<ChatProp> = (props: ChatProp) => {
    const groupChat: GroupChat = props.groupChat;

    const messageRef = useRef<HTMLInputElement>();

    const [chatSettingsMenu, setChatSettingsMenu] = useState<boolean>(false);

    const stompClient = useRef<Client>(null);

    const stompSubscription = useRef<Subscription>(null);

    const stompErrorSubscription = useRef<Subscription>(null);

    const [errorMessage, setErrorMessage] = useState<string>('');

    const [newMessages, setNewMessages] = useState<Message[]>([]);

    const [oldMessages, setOldMessages] = useState<Message[]>([]);

    const [pageNum, setPageNum] = useState<number>(0);

    const [hasMore, setHasMore] = useState<boolean>(true);

    const subscriptionStart = useRef<number>(Date.now());

    const [searchedUser, setSearchedUser] = useState<User>(null);

    const [notFoundUsername, setNotFoundUsername] = useState<String>('');

    const renameRef = useRef<HTMLInputElement>();

    const searchRef = useRef<HTMLInputElement>();

    const [lastMessageSent, setLastMessageSent] = useState<number>(0);

    const [currentFileUpload, setCurrentFileUpload] = useState<File>(null);

    const fileUploadRef = useRef<HTMLInputElement>();

    useEffect(() => {
        stompClient.current = Stomp.over(new SockJS("/ws"));
        const client: Client = stompClient.current;
        client.connect({}, onConnected, onError);
        return () => {
            if (stompSubscription.current) {
                stompSubscription.current.unsubscribe();
            }
            if (stompErrorSubscription.current) {
                stompErrorSubscription.current.unsubscribe();
            }
            if (client.connected) {
                client.disconnect(() => {}, {});
            }
        }
    }, []);

    const onConnected = () => {
        const client: Client = stompClient.current;
        stompSubscription.current = client.subscribe(`/topic/groupchat/${groupChat.id}`, onMessageReceived);
        stompErrorSubscription.current = client.subscribe(`/user/topic/errors`, onErrorReceived);
        subscriptionStart.current = Date.now();
        loadMessages();
    }

    const onError = () => {
        alert("Error in connecting to web socket, try again. Please contact me if this persists.");
        loadMessages();
    }

    const onMessageReceived = async(res: Stomp.Message) => {
        const message: Message = JSON.parse(res.body);
        if (['USER_JOIN', 'USER_LEAVE', 'USER_RENAME'].includes(message.messageType)) {
            props.refreshChats(true);
        }
        setNewMessages(newMessages => [...newMessages, message]);
    }

    const onErrorReceived = (res: Stomp.Message) => {
        setErrorMessage(res.body);
        setTimeout(() => {setErrorMessage('')}, 3000);
    }

    const handleSubmit = (e: React.FormEvent) => {
        e.preventDefault();
    }

    const handleFileUpload = () => {
        if (fileUploadRef.current.files.length > 0) {
            const file = fileUploadRef.current.files[0];
            if (!file) {
                return;
            }
            if (!["image/jpeg", "image/png", "image/gif"].includes(file.type)) {
                fileUploadRef.current.value = "";
                setCurrentFileUpload(null);
                setErrorMessage("File must be a JPEG, PNG, or GIF.");
                setTimeout(() => {setErrorMessage('')}, 3000);
            } else if (file.size > 10*1024*1024) {
                fileUploadRef.current.value = "";
                setCurrentFileUpload(null);
                setErrorMessage("File must be less than 10MB.");
                setTimeout(() => {setErrorMessage('')}, 3000);
            } else {
                setCurrentFileUpload(file);
            }
        }
    }

    const closeFileUpload = () => {
        fileUploadRef.current.value = "";
        setCurrentFileUpload(null);
    }

    const sendMessage = async () => {
        const curr: number = Date.now();
        const client: Client = stompClient.current;
        if (curr-lastMessageSent < 1000) {
            setErrorMessage("Please wait 1000ms before sending another message.");
            setTimeout(() => {setErrorMessage('')}, 1000);
        } else {
            const message = {
                content: messageRef.current.value,
                file: null as string,
                fileName: null as string,
            };
            const getFileDataURL = (file: File) => {
                return new Promise<string>(resolve => {
                    const fileReader = new FileReader();
                    fileReader.readAsDataURL(file);
                    fileReader.onloadend = () => {
                        resolve(fileReader.result as string);
                    }
                });
            }
            if (currentFileUpload) {
                message.file = await getFileDataURL(currentFileUpload);
                message.fileName = currentFileUpload.name;
            }
            client.send(`/app/send/${groupChat.id}`, {}, JSON.stringify(message));
            messageRef.current.value = "";
            fileUploadRef.current.value = "";
            setCurrentFileUpload(null);
            setLastMessageSent(curr);
        }
    }

    const loadMessages = () => {
        axios
            .get(`/api/message/${groupChat.id}/get`, {
                params: {
                        pageSize: 30,
                        pageNum: pageNum,
                        before: subscriptionStart.current
                    }
            })
            .then((res: AxiosResponse) => {
                checkRedirect(res);
                const data = res.data;
                const messages: Message[] = data.content;
                setPageNum(curr => curr+1);
                setOldMessages(oldMessages => [...oldMessages, ...messages]);
                setHasMore(data.hasNext);
            })
            .catch((e: AxiosError) => {
                checkError(e);
            })
    }

    const searchUser = () => {
        const username = searchRef.current.value;
        axios
            .get("/api/users/search", {
                params: {username: username}
            })
            .then((res: AxiosResponse) => {
                checkRedirect(res);
                const data = res.data;
                const user: User = data.user;
                if (user == null) {
                    setSearchedUser(null);
                    setNotFoundUsername(username);
                } else {
                    setSearchedUser(user);
                    setNotFoundUsername('');
                }
            })
            .catch((e: AxiosError) => {
                checkError(e);
            })
    }

    const renameChat = () => {
        const client: Client = stompClient.current;
        const form = {
            name: renameRef.current.value
        };
        client.send(`/app/update/${groupChat.id}/rename`, {}, JSON.stringify(form));
        renameRef.current.value = "";
    }

    const addUser = () => {
        const client: Client = stompClient.current;
        const form = {
            username: searchedUser.username
        };
        client.send(`/app/update/${groupChat.id}/users/add`, {}, JSON.stringify(form));
        searchRef.current.value = "";
        setSearchedUser(null);
    }

    const leaveChat = () => {
        const client: Client = stompClient.current;
        client.send(`/app/update/${groupChat.id}/users/remove`, {}, "");
    }

    
    return(
    <>
        <div className="p-2 d-flex flex-column h-100">
            <div className="fs-3 fw-bold border-bottom text-center text-break">
                {groupChat.name} { chatSettingsMenu && "- Settings"}
            </div>
            <div className="d-flex justify-content-center my-1">
                <button className="btn btn-light btn-sm" onClick={() => setChatSettingsMenu(!chatSettingsMenu)}>
                    {(chatSettingsMenu) ? "Chat" : "Chat Settings"}
                </button>
            </div>
            {errorMessage && <div className="bg-danger p-3 m-2">{errorMessage}</div>}
            {
                (chatSettingsMenu) ?
                <>
                    <div className="flex-grow overflow-auto">
                        <div className="py-3 px-4">
                            <div className="fs-3 text-center text-decoration-underline">
                                Rename Chat
                            </div>
                            <form onSubmit={handleSubmit}>
                                <br/>
                                <input type="text" className="form-control" name="rename" placeholder={groupChat.name} ref={renameRef} autoComplete='off'/>
                                <br/>
                                <button className="btn-success btn mx-2" onClick={renameChat}>Rename</button>
                            </form>
                        </div>
                        <div className="py-3 px-4">
                            <div className="fs-3 text-center text-decoration-underline">
                                Add Member
                            </div>
                            <form onSubmit={handleSubmit}>
                                {notFoundUsername && <div className="bg-danger p-3 m-2">User '{notFoundUsername}' does not exist.</div>}
                                <br/>
                                <input type="text" className="form-control" name="username" placeholder="Search User" ref={searchRef} autoComplete='off'/>
                                <br/>
                                <button className="btn-success btn mx-2" onClick={searchUser}>Search</button>
                                {searchedUser && 
                                    <>
                                        <br/>
                                        <UsersView users={[searchedUser]}/>
                                        <br/>
                                        <button className="btn-success btn mx-2" onClick={addUser}>Add User</button>
                                    </>}
                            </form>
                        </div>
                        <div className="py-3 px-4">
                            <div className="fs-3 text-center text-decoration-underline">
                                Chat Members
                            </div>
                            <UsersView users={groupChat.users}/>
                        </div>
                        <div className="py-3 px-4">
                            <button className="btn-danger btn mx-2" onClick={leaveChat}>Leave Chat</button>
                        </div>
                    </div>
                </>
                :
                <>
                    <div className="flex-grow-1 overflow-auto d-flex flex-column-reverse" style={{background: "#272929"}} id="messagesScroll">
                        <div>
                            <InfiniteScroll
                                dataLength={oldMessages.length}
                                next={loadMessages}
                                className="d-flex flex-column-reverse"
                                inverse={true}
                                hasMore={hasMore}
                                loader={
                                    <div className="d-flex justify-content-center my-3" key={0}>
                                        <div className="spinner-border text-light" style={{width: "3rem", height: "3rem"}} role="status"></div>
                                    </div>}
                                endMessage={
                                    <div className="fs-5 p-4" key={0}>
                                        This is the beginning of the conversation.
                                    </div>}
                                scrollableTarget="messagesScroll">
                                {
                                    oldMessages.map(message => { return (
                                        <MessageView message={message} key={message.id}/>
                                    )})
                                }
                            </InfiniteScroll>
                            {
                                newMessages.map(message => { return(
                                    <MessageView message={message} key={message.id}/>
                                )})
                            }
                        </div>
                    </div>
                    {currentFileUpload && 
                    <div style={{maxWidth: "300px"}} className="px-2 my-2 bg-dark bg-gradient d-flex flex-column justify-content-between">
                        <div data-bs-theme="dark" className="d-flex justify-content-end my-1">
                            <button type="button" className="btn-close" aria-label="Close" onClick={closeFileUpload}></button>
                        </div>
                        <div className="w-100 my-1 px-2 d-flex justify-content-center">
                            <div className="d-flex justify-content-center align-content-center" style={{minHeight: "100px", maxHeight: "250px", minWidth: "250px", maxWidth: "250px"}}>
                                <img src={URL.createObjectURL(currentFileUpload)} className="my-1 object-fit-contain mh-100 mw-100"/>
                            </div>
                        </div>
                        <div className="d-flex justify-content-between my-1 fw-light" style={{fontSize: "12px"}}>
                            <span className="mx-2">{currentFileUpload.name}</span>
                            <span className="mx-2 text-nowrap">{filesize(currentFileUpload.size, {standard: "jedec"})}</span>
                        </div>
                    </div>
                    }
                    <form className="my-2 d-flex" onSubmit={handleSubmit}>
                            <input type="file" accept="image/jpeg, image/png, image/gif" onChange={handleFileUpload} ref={fileUploadRef} className="d-none"/>
                            <label className="btn btn-secondary" onClick={() => fileUploadRef.current.click()}>
                                <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" className="bi bi-upload" viewBox="0 0 16 16">
                                    <path d="M.5 9.9a.5.5 0 0 1 .5.5v2.5a1 1 0 0 0 1 1h12a1 1 0 0 0 1-1v-2.5a.5.5 0 0 1 1 0v2.5a2 2 0 0 1-2 2H2a2 2 0 0 1-2-2v-2.5a.5.5 0 0 1 .5-.5"></path>
                                    <path d="M7.646 1.146a.5.5 0 0 1 .708 0l3 3a.5.5 0 0 1-.708.708L8.5 2.707V11.5a.5.5 0 0 1-1 0V2.707L5.354 4.854a.5.5 0 1 1-.708-.708z"></path>
                                </svg>
                            </label>
                        <input type="text" className="form-control flex-grow-1 mx-1" name="message" placeholder={`Message '${groupChat.name}'`} autoComplete='off' ref={messageRef}/>
                        <button className="btn btn-secondary" onClick={sendMessage}>Send</button>
                    </form>
                </>
            }
        </div>
    </>
    )
}

export default Chat;