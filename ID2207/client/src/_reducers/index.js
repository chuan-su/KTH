import { combineReducers } from 'redux';

import { authentication } from './authentication.reducer';
import { registration } from './registration.reducer';
import { users } from './users.reducer';
import { alert } from './alert.reducer';
import { eprrequest } from './epr.reducer';
import { projrequest } from './proj.reducer';

const rootReducer = combineReducers({
  authentication,
  registration,
  users,
  alert,
  eprrequest,
  projrequest
});

export default rootReducer;