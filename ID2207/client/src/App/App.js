import React from 'react';
import { Router, Route, NavLink} from 'react-router-dom';
import { connect } from 'react-redux';

import { history } from '../_helpers';
import { alertActions } from '../_actions';
import { PrivateRoute } from '../_components';
import { Home } from '../Pages';
import { Login} from '../Pages';
import { Register } from '../Pages';
import { EPRRequest } from '../Pages';
import { FinancialRequest } from '../Pages';
import { HRRequest } from '../Pages';
import { ProjectRequest } from '../Pages';

class App extends React.Component {
    constructor(props) {
        super(props);

        const { dispatch } = this.props;
        history.listen((location, action) => {
            // clear alert on location change
            dispatch(alertActions.clear());
        });
    }

    render() {
        const { alert } = this.props;
        return (
            <div className="jumbotron">
                <div className="container">
                    <div className="col-sm-8 col-sm-offset-2">
                        {alert.message &&
                            <div className={`alert ${alert.type}`}>{alert.message}</div>
                        }
                        <Router history={history}>
                            <div>
                            <ul className="header">
                                <li><NavLink to="/">Home</NavLink></li>
                                <li><NavLink to="/epr">EPR</NavLink></li>
                                <li><NavLink to="/financial">Financial Request</NavLink></li>
                                <li><NavLink to="/hr">HR Request</NavLink></li>
                                <li><NavLink to="/project">Project Distribution</NavLink></li>
                            </ul>
                                <PrivateRoute exact path="/" component={Home} />
                                <Route path="/login" component={Login} />
                                <Route path="/register" component={Register} />
                                <Route path="/epr" component={EPRRequest} />
                                <Route path="/financial" component={FinancialRequest} />
                                <Route path="/hr" component={HRRequest} />
                                <Route path="/project" component={ProjectRequest} />
                            </div>
                        </Router>
                    </div>
                </div>
            </div>
        );
    }
}

function mapStateToProps(state) {
    const { alert } = state;
    return {
        alert
    };
}

const connectedApp = connect(mapStateToProps)(App);
export { connectedApp as App }; 