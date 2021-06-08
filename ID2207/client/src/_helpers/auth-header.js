export function authHeader() {
    // return authorization header with bearer token
    let user = JSON.parse(localStorage.getItem('user'));

    if (user && user.authToken) {
        return { 'Authorization': 'Bearer ' + user.authToken };
    } else {
        return {};
    }
}