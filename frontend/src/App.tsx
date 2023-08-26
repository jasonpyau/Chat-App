import React, { useState, useEffect, createContext } from 'react';
import axios, { AxiosError, AxiosResponse } from 'axios';
import Navbar from './components/Navbar';
import ChatApp from './components/ChatApp';
import { User } from './types/User';

export const App: React.FC<{}> = () => {

    const [user, setUser] = useState<User>();
    const [loggedIn, setLoggedIn] = useState<boolean>(false);

    useEffect(() => {
        getUser();
    }, [])

    const getUser = () => {
        axios
            .get('/api/login/user')
            .then((res: AxiosResponse) => {
                const data = res.data;
                if (data.newUser) {
                    window.location.href = "/new_user";
                } else if (data.loggedIn) {
                    const user: User = data.user;
                    setUser(user);
                    setLoggedIn(true);
                } else {
                    loggedOut();
                }
            })
            .catch(() => {
                loggedOut();
                alert("Error in login, refresh.");
            });
        const loggedOut = () => {
            setUser(null);
            setLoggedIn(false);
        }
    }

    return (
    <>
        <Navbar loggedIn={loggedIn}/>
        <ChatApp user={user} loggedIn={loggedIn} setUser={setUser}/>
    </>
    )

}

export const checkRedirect = (res: AxiosResponse): void => {
    // hacky solution but axios has no better API for this.
    if (res.status == 302 || res.status == 401 || res.request.responseURL.endsWith("/login")) {
        window.location.href = "/login?error=Session expired, please log back in.";
    } else if (res.status == 429) {
        alert("You have been rate limitted. Try again later.");
    }
}

export const checkError = (err: AxiosError): void => {
    const status: number = err.response.status;
    switch (status) {
        case 401:
            window.location.href = "/login?error=Session expired, please log back in.";
            break;
        case 406:
            alert(`Invalid input: ${JSON.stringify((err.response.data as any).reason)}`);
            break;
        case 429:
            alert("You have been rate limitted. Try again later.");
            break;
        default:
            alert("Server error, please create an GitHub issue if this persists.");
    }
}