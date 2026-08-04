import axios from "axios";

const API_URL = "http://localhost:8080/alerts";

export const getAllAlerts = async () => {
    const response = await axios.get(API_URL);
    return response.data;
};

export const getActiveAlerts = async () => {
    const response = await axios.get(`${API_URL}/active`);
    return response.data;
};

export const getRecentAlerts = async () => {
    const response = await axios.get(`${API_URL}/recent`);
    return response.data;
};

export const getActiveAlertCount = async () => {
    const response = await axios.get(`${API_URL}/active-count`);
    return response.data;
};

export const getCriticalAlertCount = async () => {
    const response = await axios.get(`${API_URL}/critical-count`);
    return response.data;
};