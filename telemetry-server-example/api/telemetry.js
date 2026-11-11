// Minimal Vercel serverless collector for GTLTagger's opt-in usage stats.
// Deploy it in any Vercel project (put this file at /api/telemetry.js), then
// set Telemetry.ENDPOINT in each project's telemetry/Telemetry.java to
// "https://<your-project>.vercel.app/api/telemetry".
//
// Privacy rules this example follows - keep them if you adapt it:
//  * It validates against a strict whitelist and drops everything else.
//  * It never reads, logs or stores the sender's IP address
//    (req.headers['x-forwarded-for'], req.socket.remoteAddress, ...).
//  * It stores only the whitelisted fields below.

const UUID = /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i;
const short = (v, max = 40) => (typeof v === 'string' ? v.slice(0, max) : null);

function clean(body) {
  if (!body || body.schema !== 1 || !UUID.test(body.installId || '')) return null;
  const s = body.settings || {};
  return {
    receivedAt: new Date().toISOString().slice(0, 10), // day precision only
    installId: body.installId.toLowerCase(),
    modVersion: short(body.modVersion),
    minecraftVersion: short(body.minecraftVersion),
    loaderVersion: short(body.loaderVersion),
    java: Number.isInteger(body.java) ? body.java : null,
    os: ['windows', 'macos', 'linux', 'other'].includes(body.os) ? body.os : 'other',
    settings: {
      leftGamemode: short(s.leftGamemode),
      rightGamemode: short(s.rightGamemode),
      leftSurface: short(s.leftSurface),
      rightSurface: short(s.rightSurface),
      tierNameFormat: short(s.tierNameFormat),
      hudEnabled: s.hudEnabled === true,
      automaticDetection: s.automaticDetection === true,
    },
  };
}

// TODO: replace with a real store (Vercel KV / Upstash Redis, Supabase, ...).
// Upserting by installId gives you "unique active installs per day".
async function store(record) {
  console.log('telemetry', JSON.stringify(record));
}

module.exports = async (req, res) => {
  if (req.method !== 'POST') return res.status(405).end();
  const record = clean(typeof req.body === 'string' ? JSON.parse(req.body || '{}') : req.body);
  if (!record) return res.status(400).end();
  await store(record);
  return res.status(204).end();
};
