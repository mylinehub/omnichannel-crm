import { MessageStatusPipe } from './message-status.pipe';

describe('MessageStatusPipe', () => {
  it('create an instance', () => {
    const pipe = new MessageStatusPipe();
    expect(pipe).toBeTruthy();
  });
});
