import config from 'config';
import { authHeader } from '../_helpers';

export const finService = {
    create,
    update,
    getByStatus,
};

function create(projectId, reason, requestingDepartment, requiredAmount) {
    const requestOptions = {
        method: 'POST',
        headers: {...authHeader(), 'Content-Type': 'application/json' },
        body: JSON.stringify({ projectId, reason, requestingDepartment, requiredAmount })
    };

    return fetch(`${config.apiUrl}/financial/requests`, requestOptions)
        .then(handleResponse)
        .then(res => {
            
            return res;
        });
}
function update(sepId, status) {
    const requestOptions = {
        method: 'PUT',
        headers: {...authHeader(), 'Content-Type': 'application/json' },
        body: JSON.stringify({ status })
    };

    return fetch(`${config.apiUrl}/financial/requests/` + sepId + '/status', requestOptions)
        .then(handleResponse)
        .then(res => {
            return res;
        });
}

function getByStatus(status) {
    const requestOptions = {
        method: 'GET',
        headers: {...authHeader(), 'Content-Type': 'application/json' },
    };

    return fetch(`${config.apiUrl}/financial/requests/${status}`, requestOptions)
        .then(handleResponse)
        .then(res =>{
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