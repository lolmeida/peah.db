package com.lolmeida.config;

import org.eclipse.microprofile.openapi.annotations.OpenAPIDefinition;
import org.eclipse.microprofile.openapi.annotations.info.Contact;
import org.eclipse.microprofile.openapi.annotations.info.Info;
import org.eclipse.microprofile.openapi.annotations.info.License;
import org.eclipse.microprofile.openapi.annotations.servers.Server;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import jakarta.ws.rs.core.Application;

@OpenAPIDefinition(
    info = @Info(
        title = "peah.db API",
        version = "1.0.0",
        description = """
            ## 🚀 Complete REST API with Advanced Monitoring
            
            **peah.db** is a comprehensive REST API built with Quarkus, featuring:
            
            ### ✨ **Key Features**
            - **Complete CRUD operations** for user management
            - **Advanced monitoring & audit system** with device detection
            - **Real-time analytics** and performance metrics
            - **Automatic request logging** with structured data
            - **Device & browser detection** from User-Agent
            - **Response headers** with monitoring information
            - **Dashboard endpoints** for comprehensive analytics
            
            ### 🔧 **Technical Stack**
            - **Java 21** with Quarkus framework
            - **MySQL/PostgreSQL** database support
            - **Bean Validation** for data integrity
            - **MapStruct** for efficient object mapping
            - **Hibernate ORM** with Panache
            - **Flyway** for database migrations
            
            ### 📊 **Monitoring Capabilities**
            - **Device Detection**: Automatically identifies browsers, OS, and device types
            - **Performance Tracking**: Response times, throughput, and bottleneck analysis
            - **Request Analytics**: Comprehensive logging with IP, headers, and client info
            - **Health Monitoring**: System health checks with detailed information
            - **Dashboard Data**: Real-time statistics and performance metrics
            
            ### 🎯 **Response Headers**
            All endpoints automatically include monitoring headers:
            - `X-Request-ID`: Unique request identifier
            - `X-Device-Type`: Client device type (Desktop, Mobile, Tablet)
            - `X-Browser`: Browser name and version
            - `X-OS`: Operating system information
            - `X-Response-Time`: Request processing time
            - `X-IP`: Client IP address
            - `X-Timestamp`: Response timestamp
            
            ### 📈 **Analytics Features**
            - **Request Logs**: Searchable by endpoint, status, device, or time
            - **Performance Metrics**: Response time distribution and slow request detection
            - **Usage Statistics**: Browser, OS, and device analytics
            - **Error Tracking**: Detailed error logging and analysis
            - **Real-time Dashboard**: Live monitoring and alerting
            
            ### 🔐 **Data Models**
            - **User Management**: Complete user lifecycle with validation
            - **Request Tracking**: Comprehensive request information capture
            - **Analytics Data**: Structured monitoring and performance data
            
            ### 🌐 **Environments**
            - **Development**: Full monitoring with hot-reload
            - **Production**: Optimized performance with comprehensive logging
            - **Testing**: In-memory database with mock data
            
            ---
            
            **Built with ❤️ using Quarkus, Java 21, and modern cloud-native technologies**
            """,
        termsOfService = "https://github.com/lolmeida/peah.db/blob/main/LICENSE",
        contact = @Contact(
            name = "peah.db Development Team",
            email = "dev@peahdb.com",
            url = "https://github.com/lolmeida/peah.db"
        ),
        license = @License(
            name = "MIT License",
            url = "https://github.com/lolmeida/peah.db/blob/main/LICENSE"
        )
    ),
    servers = {
        @Server(
            url = "http://localhost:8080",
            description = "Development server"
        ),
        @Server(
            url = "https://peahdb.lolmeida.com",
            description = "Production server"
        ),
        @Server(
            url = "https://staging-api.com",
            description = "Staging server"
        )
    },
    tags = {
        @Tag(
            name = "Users",
            description = """
                **Complete user management operations**
                
                Provides full CRUD functionality for user entities with:
                - Input validation using Bean Validation
                - Secure password handling with bcrypt hashing
                - Unique constraints on username and email
                - Automatic timestamp management
                - Comprehensive error handling
                - RESTful API design following HTTP standards
                
                **Supported Operations:**
                - Create users with validation
                - Retrieve users (all or by ID)
                - Update users (complete or partial)
                - Delete users with cascading
                - Search users by username or email
                """
        ),
        @Tag(
            name = "Monitoring", 
            description = """
                **System monitoring and request information**
                
                Provides comprehensive monitoring capabilities:
                - Real-time request information extraction
                - Device and browser detection from User-Agent
                - IP address resolution with proxy support
                - Performance metrics and response time tracking
                - System health checks with detailed status
                - Request header analysis and debugging
                - URI information and routing details
                
                **Monitoring Features:**
                - Automatic device type detection (Desktop, Mobile, Tablet)
                - Browser identification (Chrome, Firefox, Safari, Edge, Opera)
                - Operating system recognition (Windows, macOS, Linux, iOS, Android)
                - Client IP extraction with proxy header support
                - Request metadata capture and analysis
                """
        ),
        @Tag(
            name = "Logs & Analytics",
            description = """
                **Request logging, analytics and monitoring dashboard**
                
                Comprehensive logging and analytics system:
                - Request log storage and retrieval
                - Performance analytics and metrics
                - Usage statistics and trends
                - Error tracking and analysis
                - Real-time dashboard data
                - Filtering and search capabilities
                
                **Analytics Capabilities:**
                - Request filtering by endpoint, status, device, or time
                - Performance metrics with response time distribution
                - Usage statistics including browser and device analytics
                - Error rate tracking and slow request detection
                - Real-time dashboard with live metrics
                - Data export and reporting features
                
                **Dashboard Features:**
                - Live request monitoring
                - Performance trend analysis
                - Device and browser distribution
                - Geographic and demographic insights
                - Alert system for anomalies
                """
        )
    }
)
public class OpenAPIConfig extends Application {
    // Configuration class for OpenAPI specification
} 