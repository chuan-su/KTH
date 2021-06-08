import { projConstants } from '../_constants';
let mytasks = JSON.parse(localStorage.getItem('mytasks'));
let allprojs = JSON.parse(localStorage.getItem('allprojs'));
const initialState = {mytasks, allprojs } ? { loggedIn: true, mytasks, allprojs } : {};

export function projrequest(state = initialState, action) {
  switch (action.type) {
    case projConstants.NEW_PROJ_REQUEST:
      return {
        loading: true
      };
    case projConstants.PROJGETMYTASKS_SUCCESS:
      return {
        mytasks: action.mytasks
      };
    case projConstants.GETALL_PROJ_SUCCESS:
      return {
        allprojs: action.allprojs
      };
    case projConstants.NEW_PROJ_FAILURE:
      return { 
        error: action.error
      };
    default:
      return state
  }
}