import React, { useEffect, useRef, useState } from 'react';
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

    const [stompClient] = useState<Client>(Stomp.over(new SockJS("/ws")));

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

    useEffect(() => {
        stompClient.connect({}, onConnected, onError);
        return () => {
            if (stompSubscription.current) {
                stompSubscription.current.unsubscribe();
            }
            if (stompErrorSubscription.current) {
                stompErrorSubscription.current.unsubscribe();
            }
            if (stompClient.connected) {
                stompClient.disconnect(() => {}, {});
            }
        }
    }, []);

    const onConnected = () => {
        stompSubscription.current = stompClient.subscribe(`/topic/groupchat/${groupChat.id}`, onMessageReceived);
        stompErrorSubscription.current = stompClient.subscribe(`/user/topic/errors`, onErrorReceived);
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

    const sendMessage = () => {
        const message = {
            content: messageRef.current.value
        };
        stompClient.send(`/app/send/${groupChat.id}`, {}, JSON.stringify(message));
        messageRef.current.value = "";
    }

    const loadMessages = () => {
        axios
            .get(`/api/message/${groupChat.id}/get`, {
                params: {
                        pageSize: 25,
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
        const form = {
            name: renameRef.current.value
        };
        stompClient.send(`/app/update/${groupChat.id}/rename`, {}, JSON.stringify(form));
        renameRef.current.value = "";
    }

    const addUser = () => {
        const form = {
            username: searchedUser.username
        };
        stompClient.send(`/app/update/${groupChat.id}/users/add`, {}, JSON.stringify(form));
        searchRef.current.value = "";
        setSearchedUser(null);
    }

    const leaveChat = () => {
        stompClient.send(`/app/update/${groupChat.id}/users/remove`, {}, "");
    }

    
    return(
    <>
        <div className="p-2 d-flex flex-column h-100">
            <div className="fs-3 fw-bold border-bottom my-1 text-center text-break">
                {groupChat.name} { chatSettingsMenu && "- Settings"}
            </div>
            <div className="d-flex justify-content-center my-1 border-bottom">
                <button className="btn btn-dark btn-sm" onClick={() => setChatSettingsMenu(!chatSettingsMenu)}>
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
                    <form className="my-2 d-flex" onSubmit={handleSubmit}>
                        <input type="text" className="form-control flex-grow-1 mx-1" name="message" placeholder={`Message '${groupChat.name}'`} autoComplete='off' ref={messageRef}/>
                        <button className="btn btn-dark" onClick={sendMessage}>Send</button>
                    </form>
                </>
            }
        </div>
    </>
    )
}

export default Chat;