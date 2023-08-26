import React, { ChangeEvent, useEffect, useRef, useState } from 'react';
import "../css/global.css";
import { GroupChat } from '../types/GroupChat';
import SockJS from 'sockjs-client';
import Stomp, { Client, Subscription } from 'stompjs';
import { Message } from '../types/Message';
import MessageView from './MessageView';

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

    useEffect(() => {
        if (!stompClient.connected) {
            stompClient.connect({}, onConnected, () => alert("Error in connecting to web socket, try again. Please contact me if this persists."));
        } else {
            onConnected();
        }
        messageRef.current.value = "";
    }, [groupChat]);

    const onConnected = () => {
        if (stompSubscription) {
            stompSubscription.unsubscribe();
            setNewMessages([]);
        }
        setStompSubscription(stompClient.subscribe(`/topic/groupchat/${groupChat.id}`, onMessageReceived));
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
                </>
                :
                <>
                    <div className="flex-grow-1 overflow-auto d-flex flex-column-reverse" style={{background: "#272929"}} id="scroller">
                        <div>
                            {
                                newMessages.map(message => { return(
                                    <MessageView message={message} key={message.id}/>)
                                })
                            }
                        </div>
                        {/* <div id="anchor"></div> */}
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