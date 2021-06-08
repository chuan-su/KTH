import config from 'config';
import { authHeader } from '../_helpers';

export const eprService = {
    create,
    update,
    approve,
    addComment,
    updateFinance,
    reject,
    getMyEprs 
};

function create( newEpr) {
    const requestOptions = {
        method: 'POST',
        headers: { ...authHeader(), 'Content-Type': 'application/json' },
        body: JSON.stringify({ newEpr })
    };

    return fetch(`${config.apiUrl}/epr`, requestOptions)
        .then(handleResponse)
        .then(res => {
            return res;
        });
}

function update(eprId, numberofAttendees, plannedBudget) {
    const requestOptions = {
        method: 'PUT',
        headers: {...authHeader(), 'Content-Type': 'application/json' },
        body: JSON.stringify({ numberofAttendees, plannedBudget })
    };

    return fetch(`${config.apiUrl}/epr/${eprId}`, requestOptions)
        .then(handleResponse)
        .then(res => {
            return res;
        });
}

function getMyEprs() {
    const requestOptions = {
        method: 'GET',
        headers: { ...authHeader(), 'Content-Type': 'application/json' }
    };

    return fetch(`${config.apiUrl}/epr/me`, requestOptions)
    .then(handleResponse)
    .then(eprs => {
        localStorage.setItem('myeprs', JSON.stringify(eprs));
        return eprs;
    });
}

function addComment(eprId, comment) {
    const requestOptions = {
        method: 'POST',
        headers: {...authHeader(), 'Content-Type': 'application/json' },
        body: JSON.stringify({ comment })
    };

    return fetch(`${config.apiUrl}/epr/${eprId}/comments`, requestOptions)
        .then(handleResponse)
        .then(res => {
            return res;
        });
}

function updateFinance(eprId, suggestedBudget, suggestedDiscount) {
    const requestOptions = {
        method: 'PUT',
        headers: {...authHeader(), 'Content-Type': 'application/json' },
        body: JSON.stringify({ suggestedBudget, suggestedDiscount })
    };

    return fetch(`${config.apiUrl}/epr/${eprId}/finance`, requestOptions)
        .then(handleResponse)
        .then(res => {
            //location.reload(true);
            return res;
        });
}

function approve(eprId) {
    const requestOptions = {
        method: 'PUT',
        headers: {...authHeader(), 'Content-Type': 'application/json' },
    };

    return fetch(`${config.apiUrl}/epr/${eprId}/approve`, requestOptions)
        .then(handleResponse)
        .then(res => {
            return res;
        });
}


function reject(eprId) {
    const requestOptions = {
        method: 'PUT',
        headers: {...authHeader(), 'Content-Type': 'application/json' },
    };

    return fetch(`${config.apiUrl}/epr/${eprId}/reject`, requestOptions)
        .then(handleResponse)
        .then(res => {
            return res;
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