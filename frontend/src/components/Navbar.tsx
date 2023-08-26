import React from 'react';

interface NavBarProp {
    loggedIn: boolean
}

const Navbar: React.FC<NavBarProp> = (props: NavBarProp) => {
    return (
    <>
        <nav className="navbar navbar-expand-sm navbar-dark bg-black">
            <div className="container-fluid">
                <a className="navbar-brand mx-3" href="/" title="Home">
                    <img src="https://avatars.githubusercontent.com/u/113565962?v=4" alt="Logo" height="40" width="40" className="d-inline-block align-text-top rounded-circle"/>
                </a>
                {(props.loggedIn) ?
                    <form className="form-signin" method="post" action="/logout">
                        <input className="btn btn-light mx-3" type="submit" value="Log Out"/>
                    </form>
                    :   
                    <a className="btn btn-light mx-3" href="/login">
                        Login
                    </a>
                }
            </div>
        </nav>
    </>
    );
}

export default Navbar;