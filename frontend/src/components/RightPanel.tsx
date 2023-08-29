import React from 'react';
import "../css/global.css";

interface RightPanelProp extends React.PropsWithChildren {

}

const RightPanel: React.FC<RightPanelProp> = (props: RightPanelProp) => {

    return(
    <>
        <div className="rounded-end text-white" id="RightPanel">
            {props.children}
        </div>
    </>
    )
}

export default RightPanel;