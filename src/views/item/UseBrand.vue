<template>
    <div>

        <v-card>
            <v-card-title>
                <v-btn class="primary" @click="addBrand">新增品牌</v-btn>
                <v-spacer/>
                <v-text-field label="搜索" append-icon="search" hide-details v-model="key"></v-text-field>
            </v-card-title>

            <v-divider/>
            <v-data-table
                    :headers="headers"
                    :items="brands"
                    :pagination.sync="pagination"
                    :total-items="totalBrands"
                    :loading="loading"
                    class="elevation-1"
            >
                <template slot="items" slot-scope="props">
                    <td>{{ props.item.id }}</td>
                    <td class="text-xs-center">{{ props.item.name }}</td>
                    <td class="text-xs-center">

                        <img :src="props.item.image" width="120" height="50" v-if="props.item.image">
                        <span v-else>无</span>
                    </td>
                    <td class="text-xs-center">{{ props.item.letter }}</td>
                    <td class="text-xs-center">
                        <v-btn class="info" @click="editBrand(props.item)">编辑</v-btn>
                        <v-btn class="warning" @click="deleteBrand(props.item.id)">删除</v-btn>
                    </td>
                </template>
            </v-data-table>
        </v-card>

        <v-dialog v-model="dialog" width="500" persistent>
<!--        :old-brand="oldBrand"    父子通讯，把父中的isEdit以及oldBrand传递给子 -->
            <brand-form @close="closeWindow" :is-edit="isEdit" :old-brand="oldBrand"></brand-form>
        </v-dialog>

    </div>
</template>

<script>

    import BrandForm from "./BrandForm"

    export default {
        components:{
            BrandForm
        },
        name: "my-brand",
        data() {
            return {
                oldBrand:{},
                isEdit:false,
                dialog:false,
                key:"",
                totalBrands: 0, // 总条数
                brands: [], // 当前页品牌数据
                loading: true, // 是否在加载中
                pagination: {}, // 分页信息
                headers: [
                    //默认排序,不排序sortable写为false
                    {text: 'id', align: 'center', value: 'id'},
                    {text: '名称', align: 'center', sortable: false, value: 'name'},
                    {text: 'LOGO', align: 'center', sortable: false, value: 'image'},
                    {text: '首字母', align: 'center', value: 'letter'},
                    {text: '操作', align: 'center', sortable: false}
                ]
            }
        },
        mounted() { // 渲染后执行
            // 查询数据
            this.getDataFromServer();
        },
        methods: {
            getDataFromServer() { // 从服务的加载数据的方法。
                this.$http.get("/item/brand/page",
                    {
                        params: {
                            page: this.pagination.page, //页码
                            rows: this.pagination.rowsPerPage, //页容量
                            sortBy: this.pagination.sortBy, //根据什么排序
                            desc: this.pagination.descending, //排序规则,
                            key: this.key
                        }
                    }).then(({data}) => {
                    //查询成功后把总的数据赋值给brands和totalBrands
                    this.brands = data.items;
                    this.totalBrands = data.total;

                    this.loading = false;
                }).catch(resp => {
                    this.$message.error("分页查询品牌失败")
                })
            },
            addBrand(){
                this.dialog = true;
                this.isEdit = false;
                this.oldBrand = null;
            },
            closeWindow(){
                this.dialog = false;
            },
            editBrand(oldBrand){

                //在进行数据回显之前，先要，根据品牌id查询分类


                this.$http.get("/item/category/of/brand?id="+oldBrand.id)
                    .then(resp=>{

                        //把查询到的分类的数组，赋值给oldBrand中的categories属性
                        oldBrand.categories = resp.data;
                        this.dialog = true;
                        this.isEdit = true;
                        this.oldBrand = oldBrand;
                    }).catch(resp=>{
                    this.$message.error("根据品牌查询分类失败")
                })



            },
            deleteBrand(brandId){

                this.$message.confirm("您确定要删除?勿以恶小而为之")
                    .then(resp=>{
                        this.$message.success("确定")
                    }).catch(resp=>{
                    this.$message.warning("取消")
                })
            }
        },
        watch: { //监视器，判断某个属性或者对象的值是否发生变更，如果发生了变更将会自动的生效（调用handler）
            pagination: {
                deep: true,//深度监控，不仅可以监控对象的变化，还可以监控对象内部属性的变化
                handler() {
                    if (this.pagination.rowsPerPage === -1) {
                        //等于-1时为一页总元素为所有元素个数(all)
                        this.pagination.rowsPerPage = this.totalBrands
                    }
                    this.getDataFromServer();
                }
            },
            key(){
                this.getDataFromServer();
            }
        }
    }
</script>

<style scoped>

</style>



