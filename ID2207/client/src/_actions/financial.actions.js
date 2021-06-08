import { finConstants } from '../_constants';
import { finService } from '../_services';
import { alertActions } from './';
import { history } from '../_helpers';

export const finActions = {
    create,
    update,
    //getByStatus
};

function create( projectId, reason, requestingDepartment, requiredAmount ) {
    return dispatch => {
        dispatch(request({ projectId }));

        finService.create(projectId, reason, requestingDepartment, requiredAmount)
            .then(
                user => { 
                    dispatch(success(projectId));
                    //history.push('/');
                },
                error => {
                    dispatch(failure(error.toString()));
                    dispatch(alertActions.error(error.toString()));
                }
            );
    };

    function request(projectId) { return { type: finConstants.FINCREATE_REQUEST, projectId } }
    function success(projectId) { return { type: finConstants.FINCREATE_SUCCESS, projectId } }
    function failure(error) { return { type: finConstants.FINCREATE_FAILURE, error } }
}

function update() {
    //userService.logout();
    return { type: userConstants.LOGOUT };
}

/* function getByStatus(user) {
        return dispa

    };

    function request(proj) { return { type: finConstants.FINCREATE_REQUEST, proj } }
    function success(proj) { return { type: finConstants.FINCREATE_SUCCESS, proj } }
    function failure(error) { return { type: finConstants.FINCREATE_FAILURE, error } }
}
 */