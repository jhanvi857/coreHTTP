import { CodeBlock, InfoCallout, WarningCallout } from "../_components";

export default function DatabaseEnvPage() {
  return (
    <div className="space-y-10 max-w-4xl mx-auto px-4 py-8">
      {/* HEADER */}
      <div className="border-b border-gray-200 dark:border-gray-800 pb-8">
        <h1 className="text-4xl font-extrabold tracking-tight text-gray-900 dark:text-white mb-4">
          Database + Environment
        </h1>
        <p className="text-xl text-gray-600 dark:text-gray-400">
          Orchestrate your secrets, configuration, and database connections securely using NioFlow's integrated environment management.
        </p>
      </div>

      {/* SECTION: ENV MANAGEMENT */}
      <section id="env-management" className="space-y-6">
        <div className="flex items-center gap-3">
          <div className="h-8 w-8 rounded-lg bg-blue-500/10 flex items-center justify-center text-blue-500 font-bold border border-blue-500/20">
            01
          </div>
          <h2 className="text-3xl font-bold text-gray-900 dark:text-white">Environment Configuration</h2>
        </div>
        
        <p className="text-gray-600 dark:text-gray-400 leading-relaxed">
          NioFlow utilizes a built-in environment loader that automatically detects and parses <code className="bg-gray-100 dark:bg-gray-800 px-1.5 py-0.5 rounded text-sm text-blue-600 dark:text-blue-400">.env</code> files in your project root. This prevents hardcoding secrets like Supabase keys or database passwords in your source code or command-line history.
        </p>

        <div className="bg-gray-50 dark:bg-[#0f0f11] rounded-2xl border border-gray-200 dark:border-gray-800 p-6 md:p-8 space-y-6">
          <h3 className="text-xl font-bold flex items-center gap-2 text-gray-900 dark:text-white">
            <svg className="w-5 h-5 text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" /></svg>
            Library Credit & Compliance
          </h3>
          <p className="text-sm text-gray-600 dark:text-gray-400 italic">
            NioFlow's environment management is powered by the excellent <strong className="text-blue-500">dotenv-java</strong> library authored by <strong>io.github.cdimascio</strong>. We use the official, unmodified library as a dependency to ensure full security and performance standards.
          </p>
        </div>

        <InfoCallout title="Setting up your .env file">
          Create a file named <code className="text-blue-500">.env</code> in your project root. NioFlow will automatically load these variables at startup.
        </InfoCallout>

        <CodeBlock 
          language="bash"
          title=".env example"
          code={`# Server Configuration
PORT=8080
JWT_SECRET=your_super_secret_signing_key_at_least_32_chars
NIOFLOW_CORS_ORIGIN=http://localhost:3000
NIOFLOW_CHAOS_ENABLED=false
NIOFLOW_REPLAY_ENABLED=false
NIOFLOW_WATCH=false

# Postgres Configuration (Supabase)
JDBC_URL=jdbc:postgresql://your-db-host.supabase.co:5432/postgres
DB_USER=postgres
DB_PASS=your_secure_database_password

# MongoDB Configuration (Atlas)
MONGO_URI=mongodb+srv://user:pass@cluster.mongodb.net/nioflow`}
        />
      </section>

      {/* SECTION: DATABASE CONNECTION */}
      <section id="database-connection" className="space-y-6 pt-12 border-t border-gray-200 dark:border-gray-800">
        <div className="flex items-center gap-3">
          <div className="h-8 w-8 rounded-lg bg-green-500/10 flex items-center justify-center text-green-500 font-bold border border-green-500/20">
            02
          </div>
          <h2 className="text-3xl font-bold text-gray-900 dark:text-white">Built-in Persistence</h2>
        </div>

        <p className="text-gray-600 dark:text-gray-400 leading-relaxed">
          NioFlow provides first-class support for both relational and document-based storage. You can initialize these directly on your application instance.
        </p>

        <CodeBlock 
          language="java"
          title="App-style Initialization"
          code={`NioFlowApp app = new NioFlowApp();

// One-liner initialization from .env
app.initPostgres(); 
app.initMongo();

app.listen(8080);`}
        />

        <CodeBlock 
          language="java"
          title="Accessing Connections"
          code={`// Get a Postgres Connection (HikariCP)
try (Connection conn = Database.getPostgresConnection()) {
    // SQL Logic
}

// Get MongoDB Client
MongoClient mongo = Database.getMongoClient();
MongoDatabase db = mongo.getDatabase("production");`}
        />

        <WarningCallout title="Privacy & Security">
          Ensure that <code className="text-pink-500">.env</code> is added to your <code className="text-pink-500">.gitignore</code> file. NioFlow project generates this by default to prevent leaking credentials.
        </WarningCallout>
      </section>

      {/* SECTION: PROGRAMMATIC ACCESS */}
      <section id="env-api" className="space-y-6 pt-12 border-t border-gray-200 dark:border-gray-800">
        <h3 className="text-2xl font-bold text-gray-900 dark:text-white">The Env API</h3>
        <p className="text-gray-600 dark:text-gray-400 leading-relaxed">
          The framework provides a unified <code className="bg-gray-100 dark:bg-gray-800 px-1.5 py-0.5 rounded text-sm text-blue-600 dark:text-blue-400">io.github.jhanvi857.nioflow.Env</code> class to access configuration values with ease.
        </p>

        <CodeBlock 
          language="java"
          title="Accessing configuration"
          code={`// Get String with fallback
      String origin = Env.get("NIOFLOW_CORS_ORIGIN", "http://localhost:3000");

      // Feature guards
      boolean chaosEnabled = Env.getAsBoolean("NIOFLOW_CHAOS_ENABLED", false);
      boolean replayEnabled = Env.getAsBoolean("NIOFLOW_REPLAY_ENABLED", false);
      boolean watchEnabled = Env.getAsBoolean("NIOFLOW_WATCH", false);

// Get typed primitives
int port = Env.getAsInt("PORT", 8080);
boolean isDebug = Env.getAsBoolean("DEBUG_MODE", false);`}
        />
      </section>

      {/* FOOTER NAV */}
      <div className="pt-12 flex justify-between gap-4">
        <a href="/docs/auth-security" className="flex-1 p-6 rounded-2xl border border-gray-200 dark:border-gray-800 hover:bg-gray-50 dark:hover:bg-[#111] transition-all group">
          <div className="text-xs text-gray-500 uppercase tracking-widest mb-1">Previous</div>
          <div className="text-lg font-bold text-gray-900 dark:text-white group-hover:text-blue-500 transition-colors flex items-center gap-2">
            ← Auth + Security
          </div>
        </a>
        <a href="/docs/deployment" className="flex-1 p-6 rounded-2xl border border-gray-200 dark:border-gray-800 hover:bg-gray-50 dark:hover:bg-[#111] transition-all group text-right">
          <div className="text-xs text-gray-500 uppercase tracking-widest mb-1">Next</div>
          <div className="text-lg font-bold text-gray-900 dark:text-white group-hover:text-blue-500 transition-colors flex items-center justify-end gap-2">
            Operations + Deployment →
          </div>
        </a>
      </div>
    </div>
  );
}
