import config from 'config';
import { authHeader } from '../_helpers';

export const hrService = {
    getActive,
    create,
    update,
};

function getActive() {
    const requestOptions = {
        method: 'GET',
        headers: {...authHeader(), 'Content-Type': 'application/json' },
    };

    return fetch(`${config.apiUrl}/hr/recruitment/requests`, requestOptions)
        .then(handleResponse)
        .then(res => {
            
            return res;
        });
}
function create(contractType, jobDescription, jobTitle, requestingDepartment, yearsOfExperience) {
    const requestOptions = {
        method: 'POST',
        headers: {...authHeader(), 'Content-Type': 'application/json' },
        body: JSON.stringify({ contractType, jobDescription, jobTitle, requestingDepartment, yearsOfExperience })
    };

    return fetch(`${config.apiUrl}/hr/recruitment/requests`, requestOptions)
        .then(handleResponse)
        .then(res => {
            return res;
        });
}

function update(sepId, status) {
    const requestOptions = {
        method: 'PUT',
        headers: {...authHeader(), 'Content-Type': 'application/json' },
    };

    return fetch(`${config.apiUrl}/hr/recruitment/requests/${sepId}/status/${status}`, requestOptions)
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