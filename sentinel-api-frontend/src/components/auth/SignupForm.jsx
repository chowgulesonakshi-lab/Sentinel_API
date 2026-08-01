import { useState } from "react";
import { signup } from "../../services/authService";

function SignupForm({setIsLogin}) {
    const [formData, setFormData] = useState({
        name:"",
        email:"",
        password:""
    });
    const handleChange = (e)=>{
        setFormData({
            ...formData,
            [e.target.name]: e.target.value
        });
    };

    const handleSignup = async()=>{
        try{
            const response = await signup(formData);
            console.log(response.data);
            alert("Account created successfully");
            setIsLogin(true);
        }
        catch(error){
            console.log(error.response?.data);
            alert("Signup failed");
        }
    };

    return (
        <div className="w-full max-w-md">
            <h2 className="text-4xl font-bold text-slate-900 mb-3">
                Create Account
            </h2>

            <p className="text-slate-500 mb-8">
                Join Sentinel API today.
            </p>

            <input
            type="text"
            name="name"
            placeholder="Name"
            onChange={handleChange}
            className="w-full border rounded-lg p-3 mb-4"
            />

            <input
            type="email"
            name="email"
            placeholder="Email"
            onChange={handleChange}
            className="w-full border rounded-lg p-3 mb-4"
            />

            <input
            type="password"
            name="password"
            placeholder="Password"
            onChange={handleChange}
            className="w-full border rounded-lg p-3 mb-6"
            />

            <button
            onClick={handleSignup}
            className="w-full bg-blue-600 text-white py-3 rounded-lg"
            >
            Create Account
            </button>


            <p className="text-center text-sm text-slate-500 mt-6">
                Already have an account?
                <span
                    onClick={() => setIsLogin(true)}
                    className="text-blue-600 cursor-pointer ml-1 font-medium">
                    Sign In →
                </span>
            </p>

        </div>
    );
}

export default SignupForm;