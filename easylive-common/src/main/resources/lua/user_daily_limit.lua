local limit = tonumber(ARGV[1])
local ttl = tonumber(ARGV[2])
local current = redis.call('GET', KEYS[1])

if not current then
    redis.call('SET', KEYS[1], 1)
    if ttl and ttl > 0 then
        redis.call('PEXPIRE', KEYS[1], ttl)
    end
    return 1
end

current = tonumber(current)
if current >= limit then
    return current
end

local nextCount = redis.call('INCR', KEYS[1])
if nextCount == 1 and ttl and ttl > 0 then
    redis.call('PEXPIRE', KEYS[1], ttl)
end
return nextCount
