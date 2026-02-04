import { RxStompConfig } from '@stomp/rx-stomp';

export const myRxStompConfig: RxStompConfig = {
  // Which server?
  brokerURL: 'ws://localhost:8080/chat',

  // Headers
  // Typical keys: login, passcode, host
  //auth key is set as inbound rule at back end code as per spring boot application. Any change if required here should be done from back end first.
  connectHeaders: {
    // login: 'guest',
    // passcode: 'guest',
    auth: localStorage.getItem("token"),
  },

  // How often to heartbeat?
  // Interval in milliseconds, set to 0 to disable
  heartbeatIncoming: 0, // Typical value 0 - disabled
  heartbeatOutgoing: 20000, // Typical value 20000 - every 20 seconds

  // Wait in milliseconds before attempting auto reconnect
  // Set to 0 to disable
  // Typical value 500 (500 milli seconds)
  reconnectDelay: 2000000,

  // Will log diagnostics on console
  // It can be quite verbose, not recommended in production
  // Skip this key to stop logging to console
  debug: (msg: string): void => {
    console.log(new Date(), msg);
  },
};
