import React, { ChangeEvent, useState } from 'react';
import "../css/global.css";
import { DefaultPicture, User } from '../types/User';
import axios, { AxiosError, AxiosResponse } from 'axios';
import { checkError, checkRedirect } from '../App';

interface ProfileProp {
    user: User,
    setUser: (user: User) => void,
}

const Profile: React.FC<ProfileProp> = (props: ProfileProp) => {
    const user = props.user;

    const [displayNameInput, setDisplayNameInput] = useState<string>('');

    const [success, setSuccess] = useState<boolean>(null);

    const displayNameInputChange = (e: ChangeEvent<HTMLInputElement>) => {
        setDisplayNameInput(e.target.value);
    }

    const updateDisplayName = () => {
        axios
            .patch("/api/users/update/display_name", null, {
                params: {displayName: displayNameInput}
            })
            .then((res: AxiosResponse) => {
                checkRedirect(res);
                const newUser: User = res.data.user;
                props.setUser(newUser);
                setSuccess(true);
            })
            .catch((e: AxiosError) => {
                checkError(e);
                setSuccess(false);
            })
    }

    const handleSubmit = (e: React.FormEvent) => {
        e.preventDefault();
    }

    return(
    <>
        <div className="p-5 overflow-auto h-100">
            <div className="fw-bold fs-3">
                Profile
            </div>
            <br/>
            <div>
                <img src={user.avatarURL || DefaultPicture} alt="Profile Picture" title={`@${user.username}`} height="50" width="50" className="rounded"></img>
            </div>
            <br/>
            <div className="fs-5">
                Username: <span className="fw-bold">{user.username}</span>
            </div>
            <br/>
            {(success === true) && <div className="bg-success p-3 m-2">Success</div>}
            {(success === false) && <div className="bg-danger p-3 m-2">Error</div>}
            <div className="fs-5 bw-bold my-1">
                Display Name
            </div>
            <form onSubmit={handleSubmit}>
                <input type="text" className="form-control my-2" name="displayName" placeholder={user.displayName} onChange={displayNameInputChange} autoComplete='off'/>
                <button className="btn btn-lg btn-success my-4" onClick={updateDisplayName}>Update</button>
            </form>
            <br/>
            <br/>
            <div className="fs-5">
                OAuth Provider: <span className="fw-bold">{user.authenticationProvider}</span>
            </div>
            <br/>
            <div className="fs-5">
                Role: <span className="fw-bold">{user.role}</span>
            </div>
            <br/>
            <div className="fs-5">
                Account Created: <span className="fw-bold">{new Date(user.createdAt*1000).toLocaleDateString("en-US")}</span>
            </div>
        </div>
    </>
    )
}

export default Profile;