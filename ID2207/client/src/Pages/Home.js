import React from 'react';
import { Link, Route, NavLink, HashRouter } from "react-router-dom";

import { connect } from 'react-redux';
import { userActions } from '../_actions';

class Home extends React.Component {
    componentDidMount() {
        //this.props.dispatch(userActions.getAll());
    }


    render() {
        const { user } = this.props;
        const role = user.role;
        return (
            <div className="col-md-6 col-md-offset-3">
                <p>Name: {user.username}</p>
                <p>Department: {user.department}</p>
                <p>Team: {user.team}</p>
                <p>Role: {user.role}</p>
                <p>Authtoken: {user.authToken}</p>
                <p>ID: {user.sepId}</p>
                <p>
                    <Link to="/login">Logout</Link>
                </p>

            </div>
        );
    }
}

function mapStateToProps(state) {
    const { users, authentication } = state;
    const { user } = authentication;
    return {
        user
    };
}

const connectedHome = connect(mapStateToProps)(Home);
export { connectedHome as Home };