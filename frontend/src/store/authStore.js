import { create } from 'zustand';

const useAuthStore = create((set) => ({
    accessToken: localStorage.getItem('accessToken'),
    isLoggedIn: !!localStorage.getItem('accessToken'),

    login: (accessToken, refreshToken) => {
        localStorage.setItem('accessToken', accessToken);
        localStorage.setItem('refreshToken', refreshToken);
        set({ accessToken, isLoggedIn: true });
    },

    logout: () => {
        localStorage.clear();
        set({ accessToken: null, isLoggedIn: false });
    },
}));

export default useAuthStore;
