import { useState } from "react";
import axios from "axios";
import { useNavigate } from "react-router-dom";

function LoginForm({setIsLogin}){
    const[email, setEmail] = useState("");
    const[password, setPassword] = useState("");
    const navigate = useNavigate();

            const handleLogin = async() => {
                try{
                    const response = await axios.post("http://localhost:8080/login",
                        {
                            email, password
                        });
                    console.log(response.data);
                    localStorage.setItem("token", response.data.token);
                    console.log("Navigating to dashboard...");
                    navigate("/dashboard");
                    } catch(error){
                        console.log(error);
                        alert("Login Failed");
                    }
                }
    return (
        <div className="w-full max-w-md">

            <h2 className="text-4xl font-bold text-slate-900 mb-3">
                Welcome to Sentinel API Observability Platform
            </h2>

            <p className="text-slate-500 mb-8">
                Sign in to monitor your APIs.
            </p>

            <label className="block text-sm mb-2">
                Email
            </label>

            <input
                type="email"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                placeholder="Enter your email"
                className="w-full border border-slate-300 rounded-lg p-3 mb-5 focus:outline-none focus:ring-2 focus:ring-blue-500"
            />

            <label className="block text-sm mb-2">
                Password
            </label>

            <input
                type="password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                placeholder="Enter your password"
                className="w-full border border-slate-300 rounded-lg p-3 mb-6 focus:outline-none focus:ring-2 focus:ring-blue-500"
            />

            <button
                onClick={handleLogin}
                className="w-full bg-blue-600 text-white py-3 rounded-lg font-medium hover:bg-blue-700 transition"
            >
                Sign In
            </button>

            <p className="text-center text-sm text-slate-500 mt-6">
                New to Sentinel?
                <span
                    onClick={() => setIsLogin(false)}
                    className="text-blue-600 cursor-pointer ml-1 font-medium"
                >
                    Create your account →
                </span>
            </p>

        </div>
    );
}

export default LoginForm;