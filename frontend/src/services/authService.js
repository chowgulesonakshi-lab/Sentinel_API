import api from "../api/axios";

export const signup = (userData) => {
    return api.post("/signup", userData);
};

export const login = (credentials) => {
    return api.post("/login", credentials);
};