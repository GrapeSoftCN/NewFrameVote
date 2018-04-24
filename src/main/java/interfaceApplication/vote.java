package interfaceApplication;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.bson.types.ObjectId;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import common.java.JGrapeSystem.rMsg;
import common.java.apps.appsProxy;
import common.java.interfaceModel.GrapeDBDescriptionModel;
import common.java.interfaceModel.GrapePermissionsModel;
import common.java.interfaceModel.GrapeTreeDBModel;
import common.java.nlogger.nlogger;
import common.java.time.timeHelper;

public class vote {
	private HashMap<String, Object> map = new HashMap<>();
	private GrapeTreeDBModel gDbModel;
	private JSONObject _obj = new JSONObject();
	private String pkString;

	public vote() {
		map.put("timediff", 0);
		map.put("ismulti", 0); // 是否多选 0：单选；1：多选
		map.put("isenable", 0); // 是否启用 0：不启用；1：启用
		map.put("starttime", timeHelper.nowMillis() + "");
		
		gDbModel = new GrapeTreeDBModel();
		//数据模型
		GrapeDBDescriptionModel  gdbField = new GrapeDBDescriptionModel ();
        gdbField.importDescription(appsProxy.tableConfig("vote"));
        gDbModel.descriptionModel(gdbField);
        
        //权限模型
        GrapePermissionsModel gperm = new GrapePermissionsModel();
		gperm.importDescription(appsProxy.tableConfig("vote"));
		gDbModel.permissionsModel(gperm);
		
		pkString = gDbModel.getPk();
		
        //开启检查模式
        gDbModel.checkMode();
	}

	public String VoteAdd(String info) {
		JSONObject object = AddMap(map, JSONObject.toJSON(info));
		return AddVote(object);
	}

	// 修改投票
	public String VoteUpdate(String vid, String info) {
		return resultMessage(updateVote(vid, JSONObject.toJSON(info)), "投票修改成功");
	}

	// 删除投票
	public String VoteDelete(String vid) {
		return resultMessage(deleteVote(vid), "投票删除成功");
	}

	// 批量删除投票
	public String VoteBatchDelete(String vid) {
		return resultMessage(deleteVote(vid.split(",")), "投票删除成功");
	}

	// 搜索投票
	public String VoteSearch(String info) {
		return find(JSONObject.toJSON(info));
	}

	// 分页
	public String VotePage(int idx, int pageSize) {
		return page(idx, pageSize);
	}

	// 条件分页
	public String VotePageBy(int idx, int pageSize, String info) {
		return page(idx, pageSize, JSONObject.toJSON(info));
	}

	/**
	 * 投票（增加vote中的count）
	 * 
	 * @param vid
	 *            _id
	 * @param info
	 *            投票项 {"itemid":"","itemname":"","count":""}
	 * 
	 * @return
	 */
	public String VoteSet(String vid, String info) {
		return resultMessage(votes(vid, JSONObject.toJSON(info)), "投票成功");
	}

	// 查看投票
	public String VoteCount(String _id) {
		JSONObject object = find(_id);
		if (object==null) {
			resultMessage(0, "暂无投票信息");
		}
		return resultMessage(JSONObject.toJSON(object.get("vote").toString()));
	}
	
	/**
	 * 将map添加至JSONObject中
	 * 
	 * @param map
	 * @param object
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public JSONObject AddMap(HashMap<String, Object> map, JSONObject object) {
		if (object!=null) {
			if (map.entrySet() != null) {
				Iterator<Entry<String, Object>> iterator = map.entrySet().iterator();
				while (iterator.hasNext()) {
					Map.Entry<String, Object> entry = (Map.Entry<String, Object>) iterator.next();
					if (!object.containsKey(entry.getKey())) {
						object.put(entry.getKey(), entry.getValue());
					}
				}
			}
		}
		return object;
	}
	
	public String AddVote(JSONObject object) {
		String info = "";
		if (object != null) {
			info = gDbModel.data(object).insertOnce().toString();
		}
		if (("").equals(info)) {
			resultMessage(99);
		}
		return resultMessage(find(info));
	}
	
	public String find(JSONObject fileInfo) {
		JSONArray array = null;
		if (fileInfo != null) {
			try {
				array = new JSONArray();
				for (Object object2 : fileInfo.keySet()) {
					gDbModel.eq(object2.toString(), fileInfo.get(object2.toString()));
				}
				array = gDbModel.limit(30).select();
			} catch (Exception e) {
				nlogger.logout(e);
				array = null;
			}
		}
		return resultMessage(array);
	}
	
	public JSONObject find(String vid) {
		JSONObject object = null;
		try {
			object = new JSONObject();
			object = gDbModel.eq(pkString, new ObjectId(vid)).find();
		} catch (Exception e) {
			nlogger.logout(e);
			object = null;
		}
		return object;
	}
	
	@SuppressWarnings("unchecked")
	public int updateVote(String mid, JSONObject object) {
		int code = 99;
		if (object != null) {
			try {
				if (object.containsKey("vote")) {
					object.put("vote", object.get("vote").toString());
				}
				code = gDbModel.eq(pkString, new ObjectId(mid)).data(object).updateEx() ? 0 : 99;
			} catch (Exception e) {
				nlogger.logout(e);
				code = 99;
			}
		}
		return code;
	}
	
	public int deleteVote(String mid) {
		int code = 99;
		try {
			JSONObject object = gDbModel.eq(pkString, new ObjectId(mid)).delete();
			code = (object != null ? 0 : 99);
		} catch (Exception e) {
			nlogger.logout(e);
			code = 99;
		}
		return code;
	}
	
	public int deleteVote(String[] mids) {
		int code = 99;
		try {
			gDbModel.or();
			for (int i = 0, len = mids.length; i < len; i++) {
				gDbModel.eq(pkString, new ObjectId(mids[i]));
			}
			long codes = gDbModel.deleteAll();
			code = (Integer.parseInt(String.valueOf(codes)) == mids.length ? 0 : 99);
		} catch (Exception e) {
			nlogger.logout(e);
			code = 99;
		}
		return code;
	}
	
	@SuppressWarnings("unchecked")
	public String page(int idx, int pageSize) {
		JSONObject object = null;
		try {
			JSONArray array = gDbModel.page(idx, pageSize);
			object = new JSONObject();
			object.put("totalSize", (int) Math.ceil((double) gDbModel.count() / pageSize));
			object.put("currentPage", idx);
			object.put("pageSize", pageSize);
			object.put("data", array);
		} catch (Exception e) {
			nlogger.logout(e);
			object = null;
		}
		return resultMessage(object);
	}

	@SuppressWarnings("unchecked")
	public String page(int idx, int pageSize, JSONObject fileInfo) {
		JSONObject object = null;
		if (fileInfo != null) {
			try {
				for (Object object2 : fileInfo.keySet()) {
					if (pkString.equals(object2.toString())) {
						gDbModel.eq(pkString, new ObjectId(fileInfo.get(pkString).toString()));
					}
					gDbModel.eq(object2.toString(), fileInfo.get(object2.toString()));
				}
				JSONArray array = gDbModel.dirty().page(idx, pageSize);
				object = new JSONObject();
				object.put("totalSize", (int) Math.ceil((double) gDbModel.count() / pageSize));
				object.put("currentPage", idx);
				object.put("pageSize", pageSize);
				object.put("data", array);
			} catch (Exception e) {
				object = null;
			}finally {
				gDbModel.clear();
			}
		}
		return resultMessage(object);
	}
	
	@SuppressWarnings("unchecked")
	public int votes(String vid, JSONObject object) {
		int code = 99;
		if (object != null) {
			try {
				JSONObject objects = new JSONObject();
				JSONArray newarray = new JSONArray();
				// 获取当前投票
				JSONObject _obj = find(vid);
				String votes = _obj.get("vote").toString();
				JSONArray array = JSONArray.toJSONArray(votes);
				for (int i = 0; i < array.size(); i++) {
					JSONObject object2 = (JSONObject) array.get(i);
					if (object2.get("itemid").toString().equals(object.get("itemid"))) {
						object2.put("count", Integer.parseInt(object2.get("count").toString()) + 1);
					}
					newarray.add(object2);
				}
				objects.put("vote", newarray.toString());
				code = gDbModel.eq(pkString, new ObjectId(vid)).data(objects).updateEx() ? 0 : 99;
			} catch (Exception e) {
				nlogger.logout(e);
				code = 99;
			}
		}
		return code;
	}
	
	
	public String resultMessage(int num) {
		return resultMessage(num, "");
	}

	@SuppressWarnings("unchecked")
	public String resultMessage(JSONObject object) {
		if (object == null) {
			object = new JSONObject();
		}
		_obj.put("records", object);
		return resultMessage(0, _obj.toString());
	}

	@SuppressWarnings("unchecked")
	public String resultMessage(JSONArray array) {
		if (array == null) {
			array = new JSONArray();
		}
		_obj.put("records", array);
		return resultMessage(0, _obj.toString());
	}

	public String resultMessage(int num, String message) {
		String msg = "";
		switch (num) {
		case 0:
			msg = message;
			break;
		case 1:
			msg = "必填项没有填";
			break;
		default:
			msg = "其它异常";
			break;
		}
		return rMsg.netMSG(num, msg);
	}
}
