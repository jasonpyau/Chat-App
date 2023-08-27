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
        <div className="border-top border-bottom text-white d-flex justify-content-start mx-2">
            <div className="my-3 mx-2">
                <img src={user.avatarURL || DefaultPicture} alt="Profile Picture" title={`@${user.username}`} height="50" width="50" className="rounded-circle"></img>
            </div>
            <div className="mx-2 my-3">
                <div>
                    <span className="mx-1 fw-semi-bold">{user.displayName}</span>
                    <span className="mx-1 fw-light">{date}</span>
                </div>
                <div className="mx-1 text-break">
                    {message.content}
                </div>
            </div>
        </div>
    </>
    )
}

export default MessageView;