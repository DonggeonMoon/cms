import './App.css';
import React, {useState} from "react";
import {Link} from "react-router-dom";

function Board() {
    let [title] = useState('제목');
    let [content] = useState('내용');

    return (
        <div className="Board">
            <div className="App-body">
                <div style={{color: 'red'}}>{title}</div>
                <article>{content}</article>
                <Link to="/">홈</Link>
            </div>
        </div>
    );
}

export default Board;
