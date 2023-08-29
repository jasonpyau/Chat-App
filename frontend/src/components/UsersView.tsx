import React from 'react';
import "../css/global.css";
import { DefaultPicture, User } from '../types/User';

interface UsersViewProp {
    users: User[]
}

const UsersView: React.FC<UsersViewProp> = (props: UsersViewProp) => {
    const set: Set<number> = new Set<number>();
    const users: User[] = [];
    for (const user of props.users) {
        if (!set.has(user.id)) {
            users.push(user);
            set.add(user.id);
        }
    }
    return(
    <>
        {
            users.map(user => { return(
                <div className="border-bottom d-flex flew-row" key={user.username}>
                    <div className="my-3 mx-2">
                        <img src={user.avatarURL || DefaultPicture} alt="Profile Picture" title={`@${user.username}`} height="50" width="50" className="rounded" referrerPolicy="no-referrer"></img>
                    </div>
                    <div className="my-3 mx-3">
                        <div className="fw-bold text-break">
                            {user.displayName}
                        </div>
                        <div className='text-break'>
                            @{user.username}
                        </div>
                    </div>
                </div>
            )})
        }
    </>
    )
}

export default UsersView;