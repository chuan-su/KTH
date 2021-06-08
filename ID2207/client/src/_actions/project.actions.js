import { projConstants } from '../_constants';
import { projectService } from '../_services';
import { alertActions } from './';
import { history } from '../_helpers';

export const projActions = {
    create,
    getAll,
    addTask,
    getMyTasks,
    //getByStatus
};

function create( budget, description, name ) {
    return dispatch => {
        dispatch(request({ name }));

        projectService.create(budget, description, name)
            .then(
                user => { 
                    dispatch(success(name));
                    //history.push('/');
                },
                error => {
                    dispatch(failure(error.toString()));
                    dispatch(alertActions.error(error.toString()));
                }
            );
    };

    function request(name) { return { type: projConstants.PROJCREATE_REQUEST, name } }
    function success(name) { return { type: projConstants.PROJCREATE_SUCCESS, name } }
    function failure(error) { return { type: projConstants.PROJCREATE_FAILURE, error } }
}

function getAll( ) {
    return dispatch => {
        dispatch(request());

        projectService.getAll()
            .then(
                res => { 
                    dispatch(success(res));
                    //history.push('/');
                    location.reload(true);
                },
                error => {
                    dispatch(failure(error.toString()));
                    dispatch(alertActions.error(error.toString()));
                }
            );
    };

    function request() { return { type: projConstants.PROJGETALL_REQUEST, } }
    function success(res) { return { type: projConstants.PROJGETALL_SUCCESS, res } }
    function failure(error) { return { type: projConstants.PROJGETALL_FAILURE, error } }
}

function getMyTasks( ) {
    return dispatch => {
        dispatch(request());

        projectService.getMyTasks()
            .then(
                res => { 
                    dispatch(success(res));
                    //history.push('/');
                    location.reload(true);
                },
                error => {
                    dispatch(failure(error.toString()));
                    dispatch(alertActions.error(error.toString()));
                }
            );
    };

    function request() { return { type: projConstants.PROJGETMYTASKS_REQUEST, } }
    function success(res) { return { type: projConstants.PROJGETMYTASKS_SUCCESS, res } }
    function failure(error) { return { type: projConstants.PROJGETMYTASKS_FAILURE, error } }
}
function addTask( projectId, assignedTeam, assignedUserId, description2, priority, title ) {
    let description = description2;
    return dispatch => {
        dispatch(request({ projectId }));

        projectService.addTasks(projectId, assignedTeam, assignedUserId, description, priority, title)
            .then(
                user => { 
                    dispatch(success(name));
                    //history.push('/');
                },
                error => {
                    dispatch(failure(error.toString()));
                    dispatch(alertActions.error(error.toString()));
                }
            );
    };

    function request(name) { return { type: projConstants.PROJCREATE_REQUEST, name } }
    function success(name) { return { type: projConstants.PROJCREATE_SUCCESS, name } }
    function failure(error) { return { type: projConstants.PROJCREATE_FAILURE, error } }
}

/* function getByStatus(user) {
        return dispa

    };

    function request(proj) { return { type: finConstants.FINCREATE_REQUEST, proj } }
    function success(proj) { return { type: finConstants.FINCREATE_SUCCESS, proj } }
    function failure(error) { return { type: finConstants.FINCREATE_FAILURE, error } }
}
 */