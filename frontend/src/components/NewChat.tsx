import React, { ChangeEvent, useState } from 'react';
import "../css/global.css";
import { User } from '../types/User';
import axios, { AxiosError, AxiosResponse } from 'axios';
import UsersView from './UsersView';
import { checkError, checkRedirect } from '../App';
import { GroupChat } from '../types/GroupChat';

interface NewChatProp {
    user: User,
    groupChats: GroupChat[]
    setGroupChats: (groupChats: GroupChat[]) => void
    setTab: (newTab: string) => void
}

const NewChat: React.FC<NewChatProp> = (props: NewChatProp) => {
    const [users, setUsers] = useState<User[]>([props.user]);
    const [notFoundUsername, setNotFoundUsername] = useState<String>('');

    const [chatNameInput, setChatNameInput] = useState<string>('');
    const [usernameInput, setUsernameInput] = useState<string>('');

    const chatNameInputChange = (e: ChangeEvent<HTMLInputElement>) => {
        setChatNameInput(e.target.value);
    }

    const usernameInputChange = (e: ChangeEvent<HTMLInputElement>) => {
        setUsernameInput(e.target.value);
    }

    const clear = () => {
        setUsers([props.user]);
    }

    const searchUser = () => {
        axios
            .get("/api/users/search", {
                params: {username: usernameInput}
            })
            .then((res: AxiosResponse) => {
                checkRedirect(res);
                const data = res.data;
                const user: User = data.user;
                if (user == null) {
                    setNotFoundUsername(usernameInput);
                } else {
                    setUsers(prev => [...prev, user]);
                    setNotFoundUsername('');
                }
            })
            .catch((e: AxiosError) => {
                checkError(e);
            })
    }

    const handleSubmit = (e: React.FormEvent) => {
        e.preventDefault();
    }

    const createChat = () => {
        const usernames: string[] = [];
        for (const user of users) {
            usernames.push(user.username);
        }

        axios
            .post("/api/groupchat/new", {
                name: chatNameInput,
                usernames: usernames
            })
            .then((res: AxiosResponse) => {
                const data = res.data;
                checkRedirect(res);
                const newGroupChat: GroupChat = res.data.groupChat;
                props.setGroupChats([newGroupChat, ...props.groupChats]);
                props.setTab('');
            })
            .catch((e: AxiosError) => {
                checkError(e);
            })
    }

    return(
    <>
        <div className="p-5 overflow-auto h-100">
            <div className="fw-bold fs-3">
                New Chat
            </div>
            <br/>
            <form onSubmit={handleSubmit}>
                <div>
                    Chat Name
                </div>
                <input type="text" className="form-control" name="name" placeholder="Name" onChange={chatNameInputChange} autoComplete='off'/>
                <br/>
                <div>
                    Add Users
                </div>
                {notFoundUsername && <div className="bg-danger p-3 m-2">User '{notFoundUsername}' does not exist.</div>}
                <input type="text" className="form-control" name="username" placeholder="Search User" onChange={usernameInputChange} autoComplete='off'/>
                <div className="my-2">
                    <button className="btn-success btn mx-2" onClick={searchUser}>Search</button>
                    <button className="btn-light btn mx-2" onClick={clear}>Clear</button>
                </div>
                <UsersView users={users}/>
                <button className="btn btn-lg btn-success my-4" onClick={createChat}>Create</button>
            </form>
        </div>
    </>
    )
}

export default NewChat;