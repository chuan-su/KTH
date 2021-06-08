import { eprConstants } from '../_constants';
let myeprs = JSON.parse(localStorage.getItem('myeprs'));
const initialState = myeprs ? { loggedIn: true, myeprs } : {};

export function eprrequest(state = initialState, action) {
  switch (action.type) {
    case eprConstants.EPR_CREATE:
      return {
        loading: true
      };
    case eprConstants.EPR_SUCCESS:
      return {
        items: action.users
      };
    case eprConstants.GETMYEPRS_SUCCESS:
      return {
        myeprs: action.myeprs
      };
    case eprConstants.EPR_FAILURE:
      return { 
        error: action.error
      };
    default:
      return state
  }
}