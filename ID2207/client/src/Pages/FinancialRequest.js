import React from "react";
import { connect } from 'react-redux';
import { finActions } from "../_actions/financial.actions";

class FinancialRequest extends React.Component {
    constructor(props) {
        super(props);

        this.state = {
            projectId: '',
            reason: '',
            requestingDepartment: '',
            requiredAmount: 0,
            test: ''
        };

        this.handleChange = this.handleChange.bind(this);
        this.handleSubmit = this.handleSubmit.bind(this);
    }

    handleChange(e) {
        const { name, value } = e.target;
        this.setState({ [name]: value });
    }

    handleSubmit(e) {
        e.preventDefault();

        this.setState({ submitted: true });
        const { projectId, reason, requestingDepartment, requiredAmount } = this.state;
        const { dispatch } = this.props;
        if ( projectId && reason && requestingDepartment && requiredAmount) {
            dispatch(finActions.create( projectId, reason, requestingDepartment, requiredAmount));
        }
    }

    newFinancialRequest(user) {
        const{projectId, reason, requestingDepartment, requiredAmount} = this.state;
        if(user.role=='PRODUCTION_MANAGER' ){
            return <div className="col-md-6 col-md-offset-3">
                        <h2>Create new</h2>
                        <form name="form" onSubmit={this.handleSubmit}>
                            <div className='form-group'>
                                <label htmlFor="projectId">Project ID</label>
                                <input type="text" className="form-control" name="projectId" value={projectId} onChange={this.handleChange} />

                            </div>
                            <div className='form-group'>
                                <label htmlFor="reason">Reason</label>
                                <input type="text" className="form-control" name="reason" value={reason} onChange={this.handleChange} />
                            </div>
                            <div className='form-group'>
                                <label htmlFor="requestingDepartment">Requesting Department</label>
                                <select className="form-control" name="requestingDepartment" value={requestingDepartment} onChange={this.handleChange}>
                                    <option value="CUSTOMER_SERVICE">Customer Service</option>
                                    <option value="PRODUCTION">Production</option>
                                    <option value="FINANCE">Finance</option>
                                    <option value="HR">Human Resources</option>
                                    <option value="ADMINISTRATION">Administration</option>
                                </select>
                            </div>
                            <div className='form-group'>
                                <label htmlFor="requiredAmount">Required Amount</label>
                                <input type="text" className="form-control" name="requiredAmount" value={requiredAmount} onChange={this.handleChange} />
                            </div>
                            <div className="form-group">
                                <button className="btn btn-primary">Create</button>
                            </div>
                        </form>
                    </div>
        }
        else{
           return 'No access';
        }
    }
  render() {
    const { user } = this.props;
    let newFinButton = this.newFinancialRequest(user);
    return (
      <div>
        <h2>Financial Requests</h2>
        {newFinButton}
        
        <p>Open financial requests</p>
        
        <p>Approved financial requests</p>

        <p>Rejected financial requests</p>

        <p>The form should be built up here.</p>
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

const FinancialRequest2 = connect(mapStateToProps)(FinancialRequest);
export { FinancialRequest2 as FinancialRequest };