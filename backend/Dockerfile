FROM node:22-slim
WORKDIR /srv
# طبقة الاعتماديات أولًا لتسريع إعادة البناء
COPY backend/package*.json ./backend/
RUN cd backend && npm ci --omit=dev
COPY app ./app
COPY backend ./backend
RUN mkdir -p /data && chown -R node:node /data
ENV NODE_ENV=production PORT=3000 HOST=0.0.0.0 DB_FILE=/data/safarna.db TRUST_PROXY=1
USER node
EXPOSE 3000
HEALTHCHECK --interval=30s --timeout=5s --start-period=20s CMD node -e "fetch('http://127.0.0.1:'+process.env.PORT+'/api/health').then(r=>process.exit(r.ok?0:1)).catch(()=>process.exit(1))"
WORKDIR /srv/backend
CMD ["node","--disable-warning=ExperimentalWarning","src/server.js"]
