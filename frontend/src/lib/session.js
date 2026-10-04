export const readSession = () => {
  try {
    return JSON.parse(localStorage.getItem('eventUser'));
  } catch {
    return null;
  }
};

export const saveSession = (user) => {
  localStorage.setItem('eventUser', JSON.stringify(user));
};

export const clearSession = () => {
  localStorage.removeItem('eventUser');
};
