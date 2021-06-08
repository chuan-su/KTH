import config from 'config';
import { authHeader } from '../_helpers';

export const userService = {
    login,
    logout,
    //register,
    getById,
    findUsersByTeam,
    delete: _delete
};

function login(username, password) {
    const requestOptions = {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ username, password })
    };

    return fetch(`${config.apiUrl}/auth`, requestOptions)
        .then(handleResponse)
        .then(user => {
            // login successful if there's a jwt token in the response
            if (user.authToken) {
                // store user details and jwt token in local storage to keep user logged in between page refreshes
                localStorage.setItem('user', JSON.stringify(user));
            }

            return user;
        });
}

function findUsersByTeam(team) {
    const requestOptions = {
        method: 'GET',
        headers: {...authHeader(), 'Content-Type': 'application/json' },
    };

    return fetch(`${config.apiUrl}/users/${team}`, requestOptions)
        .then(handleResponse)
        .then(res => {
            
            return res;
        });
}

function logout() {
    // remove user and personal items from local storage to log user out
    localStorage.removeItem('user');
    localStorage.removeItem('myeprs');
}

function getById(id) {
    const requestOptions = {
        method: 'GET',
        headers: authHeader()
    };

    return fetch(`${config.apiUrl}/users/${id}`, requestOptions).then(handleResponse);
}

/* function register(user) {
    const requestOptions = {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(user)
    };

    return fetch(`${config.apiUrl}/users/register`, requestOptions).then(handleResponse);
} */
function _delete(id) {
    const requestOptions = {
        method: 'DELETE',
        headers: authHeader()
    };

    return fetch(`${config.apiUrl}/users/${id}`, requestOptions).then(handleResponse);
}

function handleResponse(response) {
    return response.text().then(text => {
        const data = text && JSON.parse(text);
        if (!response.ok) {
            if (response.status === 401) {
                location.reload(true);
            }

            const error = (data && data.message) || response.statusText;
            return Promise.reject(error);
        }

        return data;
    });
}