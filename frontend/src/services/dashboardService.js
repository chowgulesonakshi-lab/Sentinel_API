import axios from "axios";

const API_URL = "http://localhost:8080";
const getAuthHeader = () => {
    const token = localStorage.getItem("token");
    return {
        headers: {
            Authorization: `Bearer ${token}`,
        },
    };
};

export const getDashboard = async () => {
    const response = await axios.get(
        `${API_URL}/dashboard`,
        getAuthHeader()
    );
    return response.data;
};


export const getApiHealth = async () => {
    const response = await axios.get(
        `${API_URL}/dashboard/health`,
        getAuthHeader()
    );

    return response.data;
};

export const getRecentMetrics = async () => {
    const response = await axios.get(
        `${API_URL}/dashboard/recent-metrics`,
        getAuthHeader()
    );

    return response.data;
};