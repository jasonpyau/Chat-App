import React from 'react';
import "../css/global.css";

interface LeftPanelProp extends React.PropsWithChildren {

}

const LeftPanel: React.FC<LeftPanelProp> = (props: LeftPanelProp) => {

    return(
    <>
        <div className="bg-light rounded-start overflow-auto" id="LeftPanel">
            {props.children}
        </div>
    </>
    )
}

export default LeftPanel;