import React from 'react';
import "../css/global.css";
import { Message } from '../types/Message';
import { DefaultPicture, User } from '../types/User';

interface MessageViewProp {
    message: Message
}

const MessageView: React.FC<MessageViewProp> = (props: MessageViewProp) => {
    const message: Message = props.message;
    const user: User = message.sender;
    const date: string = new Date(message.createdAt*1000)
                                .toLocaleString(undefined, {
                                    year: 'numeric', 
                                    month: 'numeric', 
                                    day: 'numeric', 
                                    hour: 'numeric', 
                                    minute: 'numeric', 
                                    second: 'numeric'
                                });
    return(
    <>
        {message.messageType === 'USER_CHAT' &&
            <div className="border-top text-white d-flex justify-content-start mx-2">
                <div className="my-3 mx-2">
                    <img src={user.avatarURL || DefaultPicture} alt="Profile Picture" title={`@${user.username}`} height="50" width="50" className="rounded-circle" referrerPolicy="no-referrer"></img>
                </div>
                <div className="mx-2 my-3">
                    <div>
                        <span className="mx-1 fs-5 text-info fw-bold text-break">{user.displayName}</span>
                        <span className="mx-1 fw-light text-break">{date}</span>
                    </div>
                    <div className="mx-1 text-break">
                        {message.content}
                    </div>
                </div>
            </div>
        }
        {['USER_JOIN', 'USER_LEAVE', 'USER_RENAME'].includes(message.messageType) &&
            <div className="border-top text-white text-center mx-2">
                <div className="my-3">
                    <div className="fw-light text-break">
                        {date}
                    </div>
                    <div className="text-break fst-italic fw-medium">
                        {message.content}
                    </div>
                </div>
            </div>
        }
    </>
    )
}

export default MessageView;