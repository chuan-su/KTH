import { eprConstants } from '../_constants';
import { eprService } from '../_services';
import { alertActions } from './';
import { history } from '../_helpers';

export const eprActions = {
    create,
    //update,
    //approve,
    //aCdcomment,
    //updateFinance,
    //reject,
    getMyEprs 
};

function create( newEpr) {
    return dispatch => {
        dispatch(request({ newEpr }));

        eprService.create( newEpr)
            .then(
                res => { 
                    dispatch(success(newEpr));
                },
                error => {
                    dispatch(failure(error.toString()));
                    dispatch(alertActions.error(error.toString()));
                }
            );
    };

    function request(newEpr) { return { type: eprConstants.EPR_CREATE, newEpr } }
    function success(newEpr) { return { type: eprConstants.EPR_SUCCESS, newEpr } }
    function failure(err) { return { type: eprConstants.EPR_FAILURE, err } }
}

function getMyEprs() {
    return dispatch => {
        dispatch(request());

        eprService.getMyEprs()
            .then(
                res => { 
                    dispatch(success(res));
                    //history.push('/');
                    location.reload(true);
                    return res;
                },
                error => {
                    dispatch(failure(error.toString()));
                    dispatch(alertActions.error(error.toString()));
                }
            );
    };
    function request() { return { type: eprConstants.EPR_CREATE,  } }
    function success(res) { return { type: eprConstants.GETMYEPRS_SUCCESS, res } }
    function failure(err) { return { type: eprConstants.EPR_FAILURE, err } }
}
/*
function register(user) {
    return dispatch => {
        dispatch(request(user));

        userService.register(user)
            .then(
                user => { 
                    dispatch(success());
                    history.push('/login');
                    dispatch(alertActions.success('Registration successful'));
                },
                error => {
                    dispatch(failure(error.toString()));
                    dispatch(alertActions.error(error.toString()));
                }
            );
    };

    function request(user) { return { type: userConstants.REGISTER_REQUEST, user } }
    function success(user) { return { type: userConstants.REGISTER_SUCCESS, user } }
    function failure(error) { return { type: userConstants.REGISTER_FAILURE, error } }
}

function getAll() {
    return dispatch => {
        dispatch(request());

        userService.getAll()
            .then(
                users => dispatch(success(users)),
                error => dispatch(failure(error.toString()))
            );
    };

    function request() { return { type: userConstants.GETALL_REQUEST } }
    function success(users) { return { type: userConstants.GETALL_SUCCESS, users } }
    function failure(error) { return { type: userConstants.GETALL_FAILURE, error } }
}

// underscore + delete because delete is reserved
function _delete(id) {
    return dispatch => {
        dispatch(request(id));

        userService.delete(id)
            .then(
                user => dispatch(success(id)),
                error => dispatch(failure(id, error.toString()))
            );
    };

    function request(id) { return { type: userConstants.DELETE_REQUEST, id } }
    function success(id) { return { type: userConstants.DELETE_SUCCESS, id } }
    function failure(id, error) { return { type: userConstants.DELETE_FAILURE, id, error } }
} */