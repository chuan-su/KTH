import React from "react";
import { connect } from 'react-redux';
import { hrActions } from "../_actions/hr.actions";

class HRRequest extends React.Component {
  constructor(props) {
    super(props);

    this.state = {
        contractType: '',
        jobDescription: '',
        jobTitle: '',
        requestingDepartment: '',
        yearsOfExperience: 0,
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

    const { contractType, jobDescription, jobTitle, requestingDepartment, yearsOfExperience } = this.state;
    const { dispatch } = this.props;
    if (contractType && jobDescription && jobTitle && requestingDepartment && yearsOfExperience) {
        dispatch( hrActions.create( contractType, jobDescription, jobTitle, requestingDepartment, yearsOfExperience ));
    }
}

newRecruitmentRequestForm(user) {
  const{ contractType, jobDescription, jobTitle, requestingDepartment, yearsOfExperience} = this.state;
  if(user.role=='CUSTOMER_SERVICE_SENIOR_OFFICER' ){
      return <div className="col-md-6 col-md-offset-3">
                  <h2>Create new</h2>
                  <form name="form" onSubmit={this.handleSubmit}>
                  <div className='form-group'>
                                <label htmlFor="contractType">Contract Type</label>
                                <select className="form-control" name="contractType" value={contractType} onChange={this.handleChange}>
                                    <option value="FULL_TIME">Full Time</option>
                                    <option value="PART_TIME">Part Time</option>
                                </select>
                            </div>
                      <div className='form-group'>
                          <label htmlFor="jobDescription">Job Description</label>
                          <input type="text" className="form-control" name="jobDescription" value={jobDescription} onChange={this.handleChange} />
                      </div>
                      <div className='form-group'>
                          <label htmlFor="jobTitle">Job Title</label>
                          <input type="text" className="form-control" name="jobTitle" value={jobTitle} onChange={this.handleChange} />
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
                          <label htmlFor="yearsOfExperience">Years of Experience Title</label>
                          <input type="number" className="form-control" name="yearsOfExperience" value={yearsOfExperience} onChange={this.handleChange} />
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
    let newRecForm = this.newRecruitmentRequestForm(user);    
    return (
        <div>
        <h2>New Recruitment Request</h2>
        {newRecForm}
        
        <p>All Active Recruitment Requests</p>
        
        <p>Update Reqcruitment Request status</p>

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

const HRRequest2 = connect(mapStateToProps)(HRRequest);
export { HRRequest2 as HRRequest };