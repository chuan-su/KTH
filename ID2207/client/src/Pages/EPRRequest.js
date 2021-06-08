import React from "react";
import { connect } from 'react-redux';
import { eprActions } from "../_actions/epr.actions";

class EPRRequest extends React.Component {
    constructor(props) {
        super(props);

        this.state = {
            newEpr: {
                clientName: '',
                eventDateFrom: '',
                eventDateTo: '',
                eventType: '',
                numberOfAttendees: '',
                plannedBudget: '',
                prefdescription: '',
                prefpreferenceArea: ''
            },
            submitted: false,
        };

        this.handleChange = this.handleChange.bind(this);
        this.handleSubmit = this.handleSubmit.bind(this);
    }

    handleChange(event) {
        const { name, value } = event.target;
        const { newEpr } = this.state;
        this.setState({
            newEpr: {
                ...newEpr,
                [name]: value
            }
        });
    }

    handleSubmit(e) {
        e.preventDefault();

        const { newEpr, submitted } = this.state;
        const {clientName, eventDateFrom, eventDateTo, eventType, numberOfAttendees, plannedBudget, prefdescription, prefpreferenceArea} = newEpr;
        let EprObj = {
            clientName: clientName,
            eventDateFrom: eventDateFrom,
            eventDateTo: eventDateTo,
            eventType: eventType,
            numberOfAttendees: numberOfAttendees,
            plannedBudget: plannedBudget,
            preferences: {
                description: prefdescription,
                preferenceArea: prefpreferenceArea,
            }
        }
        this.setState({ submitted: true });
        const { dispatch } = this.props;
        if ( newEpr ) {
            dispatch(eprActions.create( EprObj));
        }
    }

    getMyEprList(){
        const { dispatch } = this.props;
        dispatch(eprActions.getMyEprs());
    }

    newEPRButton(){
        const { newEpr, submitted } = this.state;
            return <div className="col-md-6 col-md-offset-3">
                    <h2>Create new</h2>
                    <form name="form" onSubmit={this.handleSubmit}>
                        <div className='form-group'>
                            <label htmlFor="clientName">Client Name</label>
                            <input type="text" className="form-control" name="clientName" value={newEpr.clientName} onChange={this.handleChange} />
                        </div>
                        <div className='form-group'>
                            <label htmlFor="eventDateFrom">From</label>
                            <input type="text" className="form-control" name="eventDateFrom" value={newEpr.eventDateFrom} onChange={this.handleChange} />
                        </div>
                        <div className='form-group'>
                            <label htmlFor="eventDateTo">To</label>
                            <input type="text" className="form-control" name="eventDateTo" value={newEpr.eventDateTo} onChange={this.handleChange} />
                        </div>
                        <div className='form-group'>
                            <label htmlFor="eventType">Type</label>
                            <input type="text" className="form-control" name="eventType" value={newEpr.eventType} onChange={this.handleChange} />
                        </div>
                        <div className='form-group'>
                            <label htmlFor="numberOfAttendees">Number of attendees</label>
                            <input type="text" className="form-control" name="numberOfAttendees" value={newEpr.numberOfAttendees} onChange={this.handleChange} />
                        </div>
                        <div className='form-group'>
                            <label htmlFor="plannedBudget">Planned budget</label>
                            <input type="text" className="form-control" name="plannedBudget" value={newEpr.plannedBudget} onChange={this.handleChange} />
                        </div>
                        <div className='form-group'>
                            <label htmlFor="prefdescription">Description</label>
                            <input type="text" className="form-control" name="prefdescription" value={newEpr.prefdescription} onChange={this.handleChange} />
                        </div>
                        <div className='form-group'>
                            <label htmlFor="prefpreferenceArea">Preference area</label>
                            <select className="form-control" name="prefpreferenceArea" value={newEpr.prefpreferenceArea} onChange={this.handleChange}>
                                <option value="DECORATION">Decoration</option>
                                <option value="FOOD_AND_DRINK">Food and drink</option>
                                <option value="MUSIC">Music</option>
                                <option value="FILMING_AND_PHOTOS">Film & Photo</option>
                                <option value="POSTER_AND_ART_WORK">Poster and art</option>
                                <option value="COMPUTER_RELATED">Computer related</option>
                                <option value="OTHER">Other</option>
                            </select>
                            </div>
                        <div className="form-group">
                            <button className="btn btn-primary" >Create New</button>
                        </div>
                    </form>
                </div>

    }

  render() {
    const { user } = this.props;
    let canUpdate = user.role == 'CUSTOMER_SERVICE_SENIOR_OFFICER' ? true: false;
    let canApprove = user.role == 'ADMINISTRATION_MANAGER' ? true: false;
    let canReject = user.role == 'PRODUCTION_MANAGER' ? true: false;
    let canComment = user.role == false;
    let addFinance = user.role == 'FINANCIAL_MANAGER' ? true: false;
    
    let getEPRButton = '';

    let myEPRList = '';
    if(user.role != 'CUSTOMER_SERVICE_OFFICER' && user.role != 'EMPLOYEE'){
        const { myeprs } = this.props;
        if(!myeprs){
            this.getMyEprList();
        }
        else{
            console.log(myeprs);
            myEPRList = myeprs.map((k) => 
                <div className="col-md-12 col-md-offset-6">
                    <p>EPR</p>
                    <li key={k.sepId}>{k.sepId}</li>
                    <li key={Date.now()}>Created at: {k.createdAt}</li>
                </div>
            );
        }
    }


    let createEPRform = ''
    if(user.role == 'CUSTOMER_SERVICE_OFFICER'){
        createEPRform = this.newEPRButton();
    }
    else{
        createEPRform = <div className="col-md-6 col-md-offset-3">
            <p>No Access to create a new EPR</p>
        </div>
    }

    return (
      <div>
        <div className="col-md-12 col-md-offset-3">
            <h2>My EPRs</h2>
            {myEPRList}
        </div>
        
        <div style={{padding: 5 + 'em'}}></div>

        <div className="col-md-12 col-md-offset-3">
            {createEPRform}
        </div>
      </div>
    );
  }
}

function mapStateToProps(state) {
    const { eprrequest, authentication } = state;
    const { myeprs } = eprrequest;
    const { user } = authentication;
    return {
        user, myeprs
    };
}

const EPRRequest2 = connect(mapStateToProps)(EPRRequest);
export { EPRRequest2 as EPRRequest };