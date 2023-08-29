import React from 'react';
import "../css/global.css";
import { GroupChat } from '../types/GroupChat';

interface GroupChatsViewProp {
    groupChats: GroupChat[],
    tab: string,
    setTab: (newTab: string) => void,
    fetchGroupChats: () => void,
    groupChatId: number,
    setGroupChatId: (groupChatId: number) => void
}

const GroupChatsView: React.FC<GroupChatsViewProp> = (props: GroupChatsViewProp) => {
    const groupChats: GroupChat[] = props.groupChats;

    const refreshChats = () => {
        props.fetchGroupChats();
        if (props.tab === "chat") {
            props.setTab("");
        }
    }

    const setChat = (groupChat: GroupChat) => {
        props.setTab("chat");
        props.setGroupChatId(groupChat.id);
    }

    return(
    <>
        <div className="my-2 text-center">
            <div className="fs-4 fw-bold position-relative">
                Chats
                <button type="button" className="btn btn-sm btn-dark mx-2 position-absolute" onClick={refreshChats}>
                    <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" className="bi bi-arrow-clockwise" viewBox="0 0 16 16">
                        <path fillRule="evenodd" d="M8 3a5 5 0 1 0 4.546 2.914.5.5 0 0 1 .908-.417A6 6 0 1 1 8 2v1z"></path>
                        <path d="M8 4.466V.534a.25.25 0 0 1 .41-.192l2.36 1.966c.12.1.12.284 0 .384L8.41 4.658A.25.25 0 0 1 8 4.466z"></path>
                    </svg>
                </button>
            </div>
        </div>
        {
            (groupChats.length == 0) ?
                <div className="text-center">
                    No chats to load. Create a new chat or get invited to one!
                </div>   
                :
                groupChats.map(groupChat => { return(
                    <div className="border-bottom border-top text-decoration-none px-1" key={groupChat.id} onClick={() => setChat(groupChat)} style={{cursor: 'pointer', background: (groupChat.id === props.groupChatId && props.tab === 'chat') && 'Gainsboro'}}>
                        <div className="d-flex justify-content-between">
                            <div className="fs-5 text-break">
                                {groupChat.name}
                            </div>
                            <div>
                                {new Date(groupChat.lastMessageAt*1000).toLocaleDateString("en-US")}
                            </div>
                        </div>
                        <div className="my-2">
                            {
                                groupChat.users.map(user => { return(
                                    <span className="my-1 fst-italic" key={user.username}>@{user.username} </span>
                                )})
                            }
                        </div>
                    </div>
                )})
        }
    </>
    )
}

export default GroupChatsView;