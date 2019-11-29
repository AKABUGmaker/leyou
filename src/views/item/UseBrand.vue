<template>
    <div>
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
                <td class="text-xs-center"><img :src="props.item.image"></td>
                <td class="text-xs-center">{{ props.item.letter }}</td>
            </template>
        </v-data-table>
    </div>
</template>

<script>
    export default {
        name: "my-brand",
        data() {
            return {
                totalBrands: 0, // 总条数
                brands: [], // 当前页品牌数据
                loading: true, // 是否在加载中
                pagination: {}, // 分页信息
                headers: [
                    {text: 'id', align: 'center', value: 'id'},
                    {text: '名称', align: 'center', sortable: false, value: 'name'},
                    {text: 'LOGO', align: 'center', sortable: false, value: 'image'},
                    {text: '首字母', align: 'center', value: 'letter', sortable: true,}
                ]
            }
        },
        mounted(){ // 渲染后执行
            // 查询数据
            this.getDataFromServer();
        },
        methods:{
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
            },
            closeWindow(){
                this.dialog = false;
            }
        },
        watch: { //监视器，判断某个属性或者对象的值是否发生变更，如果发生了变更将会自动的生效（调用handler）
            pagination: {
                deep: true,//深度监控，不仅可以监控对象的变化，还可以监控对象内部属性的变化
                handler() {
                    //等于-1时为一页总元素为所有元素个数(all)
                    if (this.pagination.rowsPerPage === -1) {
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