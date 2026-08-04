import axios from "axios";

const API_URL = "http://localhost:8080/metrics";

export const getAllMetrics = async () => {
    const response = await axios.get(API_URL);
    return response.data;
};

export const getLatestMetrics = async () => {
    const response = await axios.get(`${API_URL}/latest`);
    return response.data;
};

export const getAverageResponseTime = async () => {
    const response = await axios.get(`${API_URL}/average-response-time`);
    return response.data;
};

export const getUpCount = async () => {
    const response = await axios.get(`${API_URL}/up-count`);
    return response.data;
};

export const getDownCount = async () => {
    const response = await axios.get(`${API_URL}/down-count`);
    return response.data;
};

export const getFailedChecksCount = async () => {
    const response = await axios.get(`${API_URL}/failed-count`);
    return response.data;
};