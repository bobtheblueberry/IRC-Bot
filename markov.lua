#!/usr/bin/lua5.1

-- parse cmd line args

local depth, length = 2, 50

local i = 1
while i <= #arg do
	if arg[i] == "-d" then
		i = i + 1
		depth = tonumber(arg[i])
	elseif arg[i] == "-n" then
		i = i + 1
		length = tonumber(arg[i])
	end
	i = i + 1
end

-- build the graph

local state = {}

function words()
	local text = io.read("*all")
	local pos = 1

	return function ()
		local s, e = string.find(text, "%S+%s*", pos)
		if s then
			pos = e + 1
			return string.sub(text, s, e)
		else
			return nil
		end
	end
end

function insert(index, value)
	if not state[index] then
		state[index] = {}
	end
	table.insert(state[index], value)
end

local ws = {}
for w in words() do
	local first = math.max(1, #ws - depth + 1)
	local key = table.concat(ws, "", first)
	insert(key, w)
	table.insert(ws, w)
end

-- generate the output

math.randomseed(os.time())
for i = 1, 5 do math.random() end

ws = {}
for i = 1, length * 2 do
	local first = math.max(1, #ws - depth + 1)
	local key = table.concat(ws, "", first)
	local list = state[key] or { "" }
	local r = math.random(#list)
	local w = list[r]
	table.insert(ws, w)

	if w == "" then break end
	if i > length * 1 then io.write(w) end
end

print()
