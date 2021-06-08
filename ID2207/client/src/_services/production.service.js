import config from 'config';
import { authHeader } from '../_helpers';

export const projectService = {
    getAll,
    create,
    addTasks,
    getMyTasks
};

function getAll() {
    const requestOptions = {
        method: 'GET',
        headers: {...authHeader(), 'Content-Type': 'application/json' },
    };

    return fetch(`${config.apiUrl}/projects`, requestOptions)
        .then(handleResponse)
        .then(res => {
            localStorage.setItem('allprojs', JSON.stringify(res));
            return res;
        });
}
function create(budget, description, name) {
    const requestOptions = {
        method: 'POST',
        headers: {...authHeader(), 'Content-Type': 'application/json' },
        body: JSON.stringify( { budget, description, name})
    };

    return fetch(`${config.apiUrl}/projects`, requestOptions)
        .then(handleResponse)
        .then(res => {
            return res;
        });
}

function addTasks(projectId, assignedTeam, assignedUserId, description, priority, title) {
    const requestOptions = {
        method: 'POST',
        headers: {...authHeader(), 'Content-Type': 'application/json' },
        body: JSON.stringify( { assignedTeam, assignedUserId, description, priority, title})
    };

    return fetch(`${config.apiUrl}/projects/${projectId}/tasks`, requestOptions)
        .then(handleResponse)
        .then(res =>{
            return res
        });
}

function getMyTasks(projectId, assignedTeam, assignedUserId, description, priority, title) {
    const requestOptions = {
        method: 'GET',
        headers: {...authHeader(), 'Content-Type': 'application/json' },
    };

    return fetch(`${config.apiUrl}/tasks/me`, requestOptions)
        .then(handleResponse)
        .then(res =>{
            localStorage.setItem('mytasks', JSON.stringify(res));
            return res
        });
}

function handleResponse(response) {
    return response.text().then(text => {
        const data = text && JSON.parse(text);
        if (!response.ok) {

            const error = (data && data.message) || response.statusText;
            return Promise.reject(error);
        }

        return data;
    });
}