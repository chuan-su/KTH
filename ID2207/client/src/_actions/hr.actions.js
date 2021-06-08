import { hrConstants } from '../_constants';
import { hrService } from '../_services';
import { alertActions } from '.';
import { history } from '../_helpers';

export const hrActions = {
    create,
    //getAll,
    //addTask,
    //getMyTasks,
    //getByStatus
};

function create( contractType, jobDescription, jobTitle, requestingDepartment, yearsOfExperience ) {
    return dispatch => {
        dispatch(request({ jobTitle }));

        hrService.create(contractType, jobDescription, jobTitle, requestingDepartment, yearsOfExperience)
            .then(
                user => { 
                    dispatch(success(jobTitle));
                    //history.push('/');
                },
                error => {
                    dispatch(failure(error.toString()));
                    dispatch(alertActions.error(error.toString()));
                }
            );
    };

    function request(jobTitle) { return { type: hrConstants.HRCREATE_REQUEST, jobTitle } }
    function success(jobTitle) { return { type: hrConstants.HRCREATE_SUCCESS, jobTitle } }
    function failure(error) { return { type: hrConstants.HRCREATE_FAILURE, error } }
}

/* function getByStatus(user) {
        return dispa

    };

    function request(proj) { return { type: finConstants.FINCREATE_REQUEST, proj } }
    function success(proj) { return { type: finConstants.FINCREATE_SUCCESS, proj } }
    function failure(error) { return { type: finConstants.FINCREATE_FAILURE, error } }
}
 */