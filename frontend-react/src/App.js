import React from 'react';
import './App.css';
import {BrowserRouter as Router, Link, Route, Routes} from 'react-router-dom'
import Board from "./Board";

function App() {
    return (
        <div className="App">
            <Router>
                <header className="App-header">
                    CMS 프로토 타입
                </header>
                <nav className="App-nav">
                    <Link to="/board">게시판</Link>
                </nav>
                <Routes>
                    <Route path="/board" element={<Board/>}/>
                </Routes>

                <footer className="App-footer">
                    ⓒ {new Date().getFullYear()}. dgmoonlabs All Rights Reserved
                </footer>

            </Router>
        </div>
    );
}

export default App;
