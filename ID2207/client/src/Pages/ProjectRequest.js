import React from "react";
import { connect } from 'react-redux';
import { projActions } from "../_actions/project.actions";
import { userActions } from "../_actions/user.actions";

class ProjectRequest extends React.Component {
  constructor(props) {
    super(props);

    this.state = {
        budget: '',
        description: '',
        name: '',

        projectId: '',
        assignedTeam: '',
        assignedUserId: '',
        description2: '',
        priority: '',
        title: '',

        teammembers: ['other'],
    };

    this.handleChange = this.handleChange.bind(this);
    this.handleSubmit = this.handleSubmit.bind(this);

    this.handleChange2 = this.handleChange2.bind(this);
    this.handleSubmit2 = this.handleSubmit2.bind(this);

}

handleChange(e) {
    const { name, value } = e.target;
    this.setState({ [name]: value });
}

handleSubmit(e) {
    e.preventDefault();

    const { budget, description, name } = this.state;
    const { dispatch } = this.props;
    if (budget && description && name) {
        dispatch( projActions.create( budget, description, name));
    }
}

handleChange2(e) {
    const { name, value } = e.target;
    if(name == 'assignedTeam'){
        //this.state.teammembers =
        this.getUsersByTeam(value);
    }
    this.setState({ [name]: value });
}

handleSubmit2(e) {
    e.preventDefault();

    const { projectId, assignedTeam, assignedUserId, description2, priority, title } = this.state;
    const { dispatch } = this.props;
    if (projectId && assignedTeam && assignedUserId && description2 && priority && title) {
        dispatch( projActions.addTask( projectId, assignedTeam, assignedUserId, description2, priority, title));
    }
}

newProjectRequestForm(user) {
  const{budget, description, name} = this.state;
  if(user.role=='PRODUCTION_MANAGER' ){
      return <div className="col-md-12 ">
                  <h2>Create new</h2>
                  <form name="form" onSubmit={this.handleSubmit}>
                      <div className='form-group'>
                          <label htmlFor="budget">Budget</label>
                          <input type="text" className="form-control" name="budget" value={budget} onChange={this.handleChange} />

                      </div>
                      <div className='form-group'>
                          <label htmlFor="description">Description</label>
                          <input type="text" className="form-control" name="description" value={description} onChange={this.handleChange} />
                      </div>
                      <div className='form-group'>
                          <label htmlFor="name">Name</label>
                          <input type="text" className="form-control" name="name" value={name} onChange={this.handleChange} />
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
newTaskForm(users) {
    const{projectId, assignedTeam, assignedUserId, description2, priority, title} = this.state;
        return <div className="col-md-12 ">
                    <h2>Add new task</h2>
                    <form name="form" onSubmit={this.handleSubmit2}>
                        <div className='form-group'>
                            <label htmlFor="projectId">projectId</label>
                            <input type="text" className="form-control" name="projectId" value={projectId} onChange={this.handleChange2} />
  
                        </div>
                        <div className='form-group'>
                                <label htmlFor="assignedTeam">assignedTeam</label>
                                <select className="form-control" name="assignedTeam" value={assignedTeam} onChange={this.handleChange2}>
                                    <option value="DECORATION">Decoration Team</option>
                                    <option value="PHOTOGRAPH">Photography Team</option>
                                    <option value="MUSIC">Music Team</option>
                                    <option value="GRAPHIC_DESIGN">Graphic Design Team</option>
                                    <option value="COMPUTER">Computer Team</option>
                                </select>
                            </div>
                        <div className='form-group'>
                            <label htmlFor="assignedUserId">assignedUserId</label>
                            <select className="form-control" name="assignedUserId" value={assignedUserId} onChange={this.handleChange2}>
                                {this.state.teammembers.map((m) => { <option value={m}>{m} </option>})}
                            </select>
{/*                             <input type="text" className="form-control" name="assignedUserId" value={assignedUserId} onChange={this.handleChange2} />
 */}                        </div>
                        <div className='form-group'>
                            <label htmlFor="description2">description2</label>
                            <input type="text" className="form-control" name="description2" value={description2} onChange={this.handleChange2} />
                        </div>
                        <div className='form-group'>
                                <label htmlFor="priority">priority</label>
                                <select className="form-control" name="priority" value={priority} onChange={this.handleChange2}>
                                    <option value="HIGH">High</option>
                                    <option value="MEDIUM">Medium</option>
                                    <option value="LOW">Low</option>
                                </select>
                            </div>
                        <div className='form-group'>
                            <label htmlFor="title">title</label>
                            <input type="text" className="form-control" name="title" value={title} onChange={this.handleChange2} />
                        </div>
                        <div className="form-group">
                            <button className="btn btn-primary">Create</button>
                        </div>
                    </form>
                </div>

  }
getUsersByTeam(team){
    const { dispatch } = this.props;
    dispatch(userActions.getUsersByTeam(team));
}
getMyTaskList(){
    const { dispatch } = this.props;
    dispatch(projActions.getMyTasks());
}
getAllProjsList(){
    const { dispatch } = this.props;
    dispatch(projActions.getAll());
}
  render() {
    const { user } = this.props;
    let myTasksList = 'Empty';
    if(user.role == 'EMPLOYEE'){
        const { mytasks } = this.props;
        if(!mytasks){
            this.getMyTaskList();
        }
        else{
            console.log(mytasks);
            myTasksList = mytasks.map((k) => 
                <div className="col-md-12 col-md-offset-3">
                    <p>Task</p>
                    <li key={k.sepId}>{k.sepId}</li>
                    <li key={Date.now()}>Created at: {k.createdAt}</li>
                </div>
            );
        }
    }

    let allProjsList = 'Empty';
    if(user.role == 'PRODUCTION_MANAGER'){
        const { allprojs } = this.props;
        if(!allprojs){
            this.getAllProjsList();
        }
        else{
            console.log(allprojs);
            allProjsList = allprojs.map((k) => 
                <div className="col-md-12 col-md-offset-3">
                    <p key={k.name + Date.now()+7}> Name: {k.name}</p>
                    <li key={k.description + Date.now()+6}>Description: {k.description}</li>
                    <li key={k.createdAt + Date.now()+5}>Created at: {k.createdAt}</li>
                    <li key={k.lastModifiedAt + Date.now()+4}>Last modified at: {k.lastModifiedAt}</li>
                    <li key={k.sepId + Date.now()+3}>Sep Id: {k.sepId}</li>
                    <li key={k.budget + Date.now()+2}>Budget: {k.budget}</li>
                    <li key={k.tasks + Date.now()+1}>Tasks: {k.tasks}</li>
                </div>
            );
        }
    }

    let newProjForm = this.newProjectRequestForm(user);    

    const { users } = this.props;
    let newTask = 'No access';
    if(user.role == 'PRODUCTION_MANAGER'){
        newTask = this.newTaskForm(users);
    }
    return (
        <div>
        <h2>Project Requests</h2>
        {newProjForm}
        
        <p>All Projects</p>
        {allProjsList}
        <p>My Tasks</p>
        {myTasksList}
        <p>Add Task</p>
        {newTask}
      </div>
    );
  }
}
function mapStateToProps(state) {
    const { users, projrequest, authentication } = state;
    const { user } = authentication;
    const { mytasks, allprojs } = projrequest;
    return {
        users,
        mytasks,
        allprojs,
        user
    };
}

const ProjectRequest2 = connect(mapStateToProps)(ProjectRequest);
export { ProjectRequest2 as ProjectRequest };