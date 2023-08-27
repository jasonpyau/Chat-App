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


interface ChatProp {
    groupChat: GroupChat
}

const Chat: React.FC<ChatProp> = (props: ChatProp) => {
    const groupChat: GroupChat = props.groupChat;

    const messageRef = useRef<HTMLInputElement>();

    const [chatSettingsMenu, setChatSettingsMenu] = useState<boolean>(false);
    
    const [stompSubscription, setStompSubscription] = useState<Subscription>(null);

    const [stompClient] = useState<Client>(Stomp.over(new SockJS("/ws")));

    const [newMessages, setNewMessages] = useState<Message[]>([]);

    const [oldMessages, setOldMessages] = useState<Message[]>([]);

    const pageNum = useRef<number>(0);
    const hasMore = useRef<boolean>(false);
    const subscriptionStart = useRef<number>(Date.now());

    useEffect(() => {
        if (!stompClient.connected) {
            stompClient.connect({}, onConnected, () => alert("Error in connecting to web socket, try again. Please contact me if this persists."));
        } else {
            onConnected();
        }
    }, [groupChat]);

    useEffect(() => {
        reset();
        loadMessages();
    }, [groupChat])

    const reset = () => {
        messageRef.current.value = "";
        pageNum.current = 0;
        setNewMessages([]);
        setOldMessages([]);
    }

    const onConnected = () => {
        if (stompSubscription) {
            stompSubscription.unsubscribe();
        }
        setStompSubscription(stompClient.subscribe(`/topic/groupchat/${groupChat.id}`, onMessageReceived));
        subscriptionStart.current = Date.now();
    }

    const onError = () => {

    }

    const onMessageReceived = (res: Stomp.Message) => {
        const message: Message = JSON.parse(res.body);
        setNewMessages(newMessages => [...newMessages, message]);
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
                            pageSize: 30,
                            pageNum: pageNum.current,
                            before: subscriptionStart.current
                    }
            })
            .then((res: AxiosResponse) => {
                checkRedirect(res);
                const data = res.data;
                const messages: Message[] = data.content;
                pageNum.current++;
                setOldMessages(oldMessages => [...oldMessages, ...messages]);
                hasMore.current = data.hasNext;
            })
            .catch((e: AxiosError) => {
                checkError(e);
            })
    }

    
    return(
    <>
        <div className="rounded-end text-white p-2 tab d-flex flex-column">
            <div className="fs-3 fw-bold border-bottom my-1 text-center">
                {groupChat.name} { chatSettingsMenu && "- Settings"}
            </div>
            <div className="d-flex justify-content-center my-1 border-bottom">
                <button className="btn btn-dark btn-sm" onClick={() => setChatSettingsMenu(!chatSettingsMenu)}>
                    {(chatSettingsMenu) ? "Chat" : "Chat Settings"}
                </button>
            </div>
            {
                (chatSettingsMenu) ?
                <>
                    <div className="flex-grow overflow-auto">
                        <div className="fs-3 text-center">
                            Chat Members
                        </div>
                        <UsersView users={groupChat.users}/>
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
                                hasMore={hasMore.current}
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