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

interface ChatAppProp {
    user: User,
    loggedIn: boolean,
    setUser: (user: User) => void
}

export const ChatApp: React.FC<ChatAppProp> = (props: ChatAppProp) => {
    const [tab, setTab] = useState<string>('');
    const [groupChat, setGroupChat] = useState<GroupChat>(null);

    const tabButtonClick = (newTab: string): void => {
        (newTab === tab && newTab != "chat") ? setTab('') : setTab(newTab);
    }

    const [groupChats, setGroupChats] = useState<GroupChat[]>([]);

    useEffect(() => {
        if (props.loggedIn) {
            getGroupChats();
        }
    }, [props.loggedIn]);

    const getGroupChats = () => {
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

    return(
        <div className='row flex-fill bg-dark'>
        {
            (props.loggedIn) ? 
                <div className="mx-auto my-auto d-flex justify-content-center row" style={{height: "90%"}}>
                    <LeftPanel user={props.user} tab={tab} setTab={tabButtonClick} groupChats={groupChats} refreshGroupChats={getGroupChats} groupChat={groupChat} setGroupChat={setGroupChat}/>
                    {tab === 'newChat' && <NewChat user={props.user} groupChats={groupChats} setGroupChats={setGroupChats} setTab={tabButtonClick}/>}
                    {tab === 'profile' && <Profile user={props.user} setUser={props.setUser}/>}
                    {tab === 'chat' && <Chat groupChat={groupChat}/>}
                    {tab === '' && <div className="rounded-end tab"></div>}
                </div>
                :
                <Welcome/>

        }
        </div>
    )
}

export default ChatApp;