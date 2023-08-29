import React, { useEffect, useState } from 'react';
import { User } from '../types/User';
import Welcome from './Welcome';
import LeftPanel from './LeftPanel';
import Chat from './Chat';
import NewChat from './NewChat';
import axios, { AxiosError, AxiosResponse } from 'axios';
import { GroupChat } from '../types/GroupChat';
import { checkError, checkRedirect } from '../App';
import Profile from './Profile';
import UsersView from './UsersView';
import GroupChatsView from './GroupChatsView';
import RightPanel from './RightPanel';

interface ChatAppProp {
    user: User,
    loggedIn: boolean,
    setUser: (user: User) => void
}

export const ChatApp: React.FC<ChatAppProp> = (props: ChatAppProp) => {
    const user = props.user;
    const [tab, setTab] = useState<string>('');
    const [groupChatId, setGroupChatId] = useState<number>();

    const tabButtonClick = (newTab: string): void => {
        (newTab === tab && newTab != "chat") ? setTab('') : setTab(newTab);
    }

    const [groupChats, setGroupChats] = useState<GroupChat[]>([]);

    useEffect(() => {
        if (props.loggedIn) {
            fetchGroupChats();
        }
    }, [props.loggedIn]);

    const fetchGroupChats = () => {
        axios
            .get("/api/groupchat/get")
            .then((res: AxiosResponse) => {
                checkRedirect(res);
                setGroupChats(res.data.groupChats);
            })
            .catch((e: AxiosError) => {
                checkError(e);
            })
    }

    const refreshChats = (fetch: boolean) => {
        if (fetch) {
            fetchGroupChats();
        } else {
            setGroupChats([...groupChats]);
        }
    }

    return(
        <div className='row flex-fill bg-dark'>
        {
            (props.loggedIn) ? 
                <div className="mx-auto my-auto d-flex justify-content-center row" style={{height: "90%"}}>
                    <LeftPanel>
                        <UsersView users={[user]}/>
                        <div className="border-bottom d-flex justify-content-evenly py-2">
                            <button className="btn btn-dark btn-sm" onClick={() => tabButtonClick("newChat")}>
                                New Chat
                            </button>
                            <button className="btn btn-dark btn-sm" onClick={() => tabButtonClick("profile")}>
                                Profile
                            </button>
                        </div>
                        <GroupChatsView groupChats={groupChats} tab={tab} setTab={setTab} fetchGroupChats={fetchGroupChats} groupChatId={groupChatId} setGroupChatId={setGroupChatId}/>
                    </LeftPanel>
                    <RightPanel>
                        {tab === 'newChat' && <NewChat user={user} groupChats={groupChats} setGroupChats={setGroupChats} setTab={setTab}/>}
                        {tab === 'profile' && <Profile user={user} setUser={props.setUser}/>}
                        {tab === 'chat' && groupChats.map(groupChat => { return (
                            groupChat.id === groupChatId && <Chat groupChat={groupChat} key={groupChat.id} refreshChats={refreshChats} user={user} setTab={setTab}/>
                        )})}
                        {tab === '' && <div className="fs-5 text-center p-5 text-wrap">Welcome to Jason Yau's Chat App!</div>}
                    </RightPanel>
                </div>
                :
                <Welcome/>

        }
        </div>
    )
}

export default ChatApp;